const express = require('express');
const cors = require('cors');
const { Pool } = require('pg');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const app = express();
const PORT = process.env.PORT || 3000;
const HOST = '0.0.0.0';

// JWT Secret resolution: Render environment variables prioritized
const JWT_SECRET = process.env.JWT_SECRET || 'echoflux_jwt_production_secret_key_2026';
if (!process.env.JWT_SECRET) {
  console.warn('WARNING: JWT_SECRET environment variable is not defined in Render! Using default fallback secret.');
}

// Enable CORS for Android client & web requests
app.use(cors());

// Parse incoming JSON payloads with a safe size limit
app.use(express.json({ limit: '64kb' }));

// Handle JSON syntax parsing errors
app.use((err, req, res, next) => {
  if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
    return res.status(400).json({
      success: false,
      error: 'Malformed JSON payload'
    });
  }
  next(err);
});

// Basic in-memory rate limiter for auth endpoints (prevents brute force / abuse)
const rateLimitMap = new Map();
const RATE_LIMIT_WINDOW_MS = 60 * 1000; // 1 minute
const MAX_AUTH_ATTEMPTS_PER_WINDOW = 20;

function authRateLimiter(req, res, next) {
  const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress || 'unknown';
  const now = Date.now();
  const clientRecord = rateLimitMap.get(clientIp) || { count: 0, resetAt: now + RATE_LIMIT_WINDOW_MS };

  if (now > clientRecord.resetAt) {
    clientRecord.count = 1;
    clientRecord.resetAt = now + RATE_LIMIT_WINDOW_MS;
  } else {
    clientRecord.count++;
  }

  rateLimitMap.set(clientIp, clientRecord);

  if (clientRecord.count > MAX_AUTH_ATTEMPTS_PER_WINDOW) {
    return res.status(429).json({
      success: false,
      error: 'Çok fazla istek gönderildi. Lütfen biraz bekleyin.'
    });
  }
  next();
}

// Periodic cleanup of rate limit map
setInterval(() => {
  const now = Date.now();
  for (const [ip, record] of rateLimitMap.entries()) {
    if (now > record.resetAt) {
      rateLimitMap.delete(ip);
    }
  }
}, 5 * 60 * 1000);

// In-Memory Storage Fallback (used when DATABASE_URL is not configured e.g. local test runner)
const memoryStore = {
  leaderboard: new Map(), // username -> { id, username, score, updated_at }
  users: new Map(),       // id -> { id, full_name, email, password_hash, created_at, updated_at }
  userEmailMap: new Map(),// email -> user
  saves: new Map(),       // userId -> { user_id, current_level, completed_levels, trophies, coins, lives, updated_at }
  nextUserId: 1
};

// Configure PostgreSQL connection via DATABASE_URL
let pool = null;

if (process.env.DATABASE_URL) {
  const isLocal = process.env.DATABASE_URL.includes('localhost') || process.env.DATABASE_URL.includes('127.0.0.1');
  pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: isLocal ? false : { rejectUnauthorized: false }
  });

  pool.on('error', (err) => {
    console.error('Unexpected error on idle PostgreSQL client:', err.message);
  });
} else {
  console.warn('NOTICE: DATABASE_URL environment variable is not defined.');
  console.warn('Running with High-Reliability In-Memory Store for testing. In production (Render), set DATABASE_URL.');
}

// Automatically create leaderboard, users, and game_saves tables and indices if database is configured
async function initDatabase() {
  if (!pool) return;
  try {
    // 1. Maintain & preserve existing leaderboard table
    const leaderboardDdl = `
      CREATE TABLE IF NOT EXISTS leaderboard (
        id SERIAL PRIMARY KEY,
        username VARCHAR(20) UNIQUE NOT NULL,
        score INT NOT NULL DEFAULT 0,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      );
      CREATE INDEX IF NOT EXISTS idx_leaderboard_score ON leaderboard (score DESC);
    `;
    await pool.query(leaderboardDdl);

    // 2. New users table: id, full_name, email (UNIQUE), password_hash, created_at, updated_at
    const usersDdl = `
      CREATE TABLE IF NOT EXISTS users (
        id SERIAL PRIMARY KEY,
        full_name VARCHAR(100) NOT NULL,
        email VARCHAR(255) UNIQUE NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      );
      CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
    `;
    await pool.query(usersDdl);

    // 3. New game_saves table: user_id (FK -> users.id), current_level, completed_levels, trophies, coins, lives, updated_at
    const gameSavesDdl = `
      CREATE TABLE IF NOT EXISTS game_saves (
        user_id INT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
        current_level INT NOT NULL DEFAULT 1,
        completed_levels JSONB NOT NULL DEFAULT '[]'::jsonb,
        trophies INT NOT NULL DEFAULT 0,
        coins INT NOT NULL DEFAULT 50,
        lives INT NOT NULL DEFAULT 5,
        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      );
      CREATE INDEX IF NOT EXISTS idx_game_saves_user_id ON game_saves (user_id);
    `;
    await pool.query(gameSavesDdl);

    console.log("Database initialized successfully: 'leaderboard', 'users', 'game_saves' tables ready.");
  } catch (error) {
    console.error("Failed to initialize database tables:", error.message);
  }
}

initDatabase();

// JWT Authentication Middleware: Validates Bearer token and extracts req.user = { userId, email }
function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({
      success: false,
      error: 'Kimlik doğrulama tokenı gerekli'
    });
  }

  jwt.verify(token, JWT_SECRET, (err, decoded) => {
    if (err) {
      return res.status(403).json({
        success: false,
        error: 'Geçersiz veya süresi dolmuş oturum tokenı'
      });
    }
    // decoded contains { userId, email }
    req.user = decoded;
    next();
  });
}

// -------------------------------------------------------------
// 1. GET /health - Service health check
// -------------------------------------------------------------
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'ok',
    database: pool ? 'postgres' : 'memory_fallback'
  });
});

// -------------------------------------------------------------
// 1.1. GET /version.json - App version and update check
// -------------------------------------------------------------
app.get('/version.json', (req, res) => {
  res.status(200).json({
    version: 2,
    latest_version: 2,
    version_name: '1.2',
    update_url: 'https://echoflux.apk.com',
    force_update: false,
    title: 'Yeni Güncelleme',
    description: 'Yeni bir ECHOFLUX güncellemesi mevcut.'
  });
});

// -------------------------------------------------------------
// 2. GET /leaderboard - Fetch top 100 highest scores
// (PRESERVED MEVCUT LEADERBOARD SİSTEMİ)
// -------------------------------------------------------------
app.get('/leaderboard', async (req, res) => {
  if (pool) {
    try {
      const query = `
        SELECT username, score
        FROM leaderboard
        ORDER BY score DESC, updated_at ASC
        LIMIT 100
      `;
      const result = await pool.query(query);
      return res.status(200).json({
        success: true,
        leaderboard: result.rows
      });
    } catch (error) {
      console.error('Error fetching leaderboard from Postgres:', error.message);
      return res.status(500).json({
        success: false,
        error: 'Failed to retrieve leaderboard from database'
      });
    }
  }

  // Memory fallback
  const list = Array.from(memoryStore.leaderboard.values())
    .sort((a, b) => b.score - a.score)
    .slice(0, 100)
    .map(e => ({ username: e.username, score: e.score }));

  return res.status(200).json({
    success: true,
    leaderboard: list
  });
});

// -------------------------------------------------------------
// 3. POST /leaderboard - Submit or update player score
// (PRESERVED MEVCUT LEADERBOARD SİSTEMİ)
// -------------------------------------------------------------
app.post('/leaderboard', async (req, res) => {
  const { username, score } = req.body || {};

  if (typeof username !== 'string') {
    return res.status(400).json({
      success: false,
      error: 'Invalid username: Must be a string'
    });
  }

  const trimmedUsername = username.trim();

  if (trimmedUsername.length === 0) {
    return res.status(400).json({
      success: false,
      error: 'Invalid username: Cannot be empty'
    });
  }

  if (trimmedUsername.length > 20) {
    return res.status(400).json({
      success: false,
      error: 'Invalid username: Maximum length is 20 characters'
    });
  }

  const containsScriptOrHtml = /<[^>]*>|[<>]|javascript:/i.test(trimmedUsername);
  if (containsScriptOrHtml) {
    return res.status(400).json({
      success: false,
      error: 'Invalid username: HTML tags or script content are not allowed'
    });
  }

  if (typeof score !== 'number' || !Number.isInteger(score)) {
    return res.status(400).json({
      success: false,
      error: 'Invalid score: Must be an integer number'
    });
  }

  if (score < 0) {
    return res.status(400).json({
      success: false,
      error: 'Invalid score: Score cannot be negative'
    });
  }

  if (score > 2147483647) {
    return res.status(400).json({
      success: false,
      error: 'Invalid score: Value exceeds maximum allowed limit'
    });
  }

  if (pool) {
    try {
      const upsertQuery = `
        INSERT INTO leaderboard (username, score, updated_at)
        VALUES ($1, $2, NOW())
        ON CONFLICT (username)
        DO UPDATE SET
          score = GREATEST(leaderboard.score, EXCLUDED.score),
          updated_at = CASE
            WHEN EXCLUDED.score > leaderboard.score THEN NOW()
            ELSE leaderboard.updated_at
          END
        RETURNING username, score;
      `;
      const result = await pool.query(upsertQuery, [trimmedUsername, score]);
      const savedEntry = result.rows[0];

      return res.status(200).json({
        success: true,
        message: 'Score recorded successfully',
        entry: {
          username: savedEntry.username,
          score: savedEntry.score
        }
      });
    } catch (error) {
      console.error('Error saving score to Postgres:', error.message);
      return res.status(500).json({
        success: false,
        error: 'Database error while saving score'
      });
    }
  }

  // Memory fallback
  const existing = memoryStore.leaderboard.get(trimmedUsername);
  if (existing) {
    if (score > existing.score) {
      existing.score = score;
      existing.updated_at = new Date();
    }
  } else {
    memoryStore.leaderboard.set(trimmedUsername, {
      id: memoryStore.leaderboard.size + 1,
      username: trimmedUsername,
      score: score,
      updated_at: new Date()
    });
  }

  const finalEntry = memoryStore.leaderboard.get(trimmedUsername);
  return res.status(200).json({
    success: true,
    message: 'Score recorded successfully',
    entry: {
      username: finalEntry.username,
      score: finalEntry.score
    }
  });
});

// -------------------------------------------------------------
// 4. POST /auth/register - Register new account
// (fullName, email, password -> id, fullName, email, token)
// -------------------------------------------------------------
app.post(['/auth/register', '/register', '/api/auth/register', '/api/register'], authRateLimiter, async (req, res) => {
  const { fullName, email, password } = req.body || {};

  // Validate fullName
  if (typeof fullName !== 'string' || fullName.trim().length < 2) {
    return res.status(400).json({
      success: false,
      error: 'Ad Soyad en az 2 karakter olmalıdır'
    });
  }
  const cleanFullName = fullName.trim();
  if (cleanFullName.length > 100) {
    return res.status(400).json({
      success: false,
      error: 'Ad Soyad en fazla 100 karakter olabilir'
    });
  }

  // Validate email
  if (typeof email !== 'string' || email.trim().length === 0) {
    return res.status(400).json({
      success: false,
      error: 'Geçerli bir e-posta adresi giriniz'
    });
  }
  const cleanEmail = email.trim().toLowerCase();
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(cleanEmail) || cleanEmail.length > 255) {
    return res.status(400).json({
      success: false,
      error: 'Lütfen geçerli bir e-posta adresi giriniz'
    });
  }

  // Validate password (min 8 characters)
  if (typeof password !== 'string' || password.length < 8) {
    return res.status(400).json({
      success: false,
      error: 'Şifre en az 8 karakter olmalıdır'
    });
  }
  if (password.length > 128) {
    return res.status(400).json({
      success: false,
      error: 'Şifre en fazla 128 karakter olabilir'
    });
  }

  try {
    const passwordHash = await bcrypt.hash(password, 12);

    if (pool) {
      // Check existing email
      const checkUser = await pool.query('SELECT id FROM users WHERE email = $1', [cleanEmail]);
      if (checkUser.rows.length > 0) {
        return res.status(409).json({
          success: false,
          error: 'Bu e-posta adresi zaten bir hesaba kayıtlı'
        });
      }

      // Insert new user
      const insertUserQuery = `
        INSERT INTO users (full_name, email, password_hash, created_at, updated_at)
        VALUES ($1, $2, $3, NOW(), NOW())
        RETURNING id, full_name, email;
      `;
      const result = await pool.query(insertUserQuery, [cleanFullName, cleanEmail, passwordHash]);
      const newUser = result.rows[0];

      // Automatically initialize game_saves record
      await pool.query(`
        INSERT INTO game_saves (user_id, current_level, completed_levels, trophies, coins, lives, updated_at)
        VALUES ($1, 1, '[]'::jsonb, 0, 50, 5, NOW())
        ON CONFLICT (user_id) DO NOTHING;
      `, [newUser.id]);

      // Sign JWT token (valid for 30 days)
      const token = jwt.sign(
        { userId: newUser.id, email: newUser.email },
        JWT_SECRET,
        { expiresIn: '30d' }
      );

      return res.status(200).json({
        success: true,
        message: 'Hesap başarıyla oluşturuldu',
        user: {
          id: newUser.id,
          fullName: newUser.full_name,
          email: newUser.email
        },
        token: token
      });
    }

    // Memory fallback
    if (memoryStore.userEmailMap.has(cleanEmail)) {
      return res.status(409).json({
        success: false,
        error: 'Bu e-posta adresi zaten bir hesaba kayıtlı'
      });
    }

    const userId = memoryStore.nextUserId++;
    const userObj = {
      id: userId,
      full_name: cleanFullName,
      email: cleanEmail,
      password_hash: passwordHash,
      created_at: new Date(),
      updated_at: new Date()
    };
    memoryStore.users.set(userId, userObj);
    memoryStore.userEmailMap.set(cleanEmail, userObj);
    memoryStore.saves.set(userId, {
      user_id: userId,
      current_level: 1,
      completed_levels: [],
      trophies: 0,
      coins: 50,
      lives: 5,
      updated_at: new Date()
    });

    const token = jwt.sign(
      { userId: userId, email: cleanEmail },
      JWT_SECRET,
      { expiresIn: '30d' }
    );

    return res.status(200).json({
      success: true,
      message: 'Hesap başarıyla oluşturuldu',
      user: {
        id: userId,
        fullName: cleanFullName,
        email: cleanEmail
      },
      token: token
    });
  } catch (error) {
    console.error('Error during /auth/register:', error.message);
    return res.status(500).json({
      success: false,
      error: 'Kayıt işlemi sırasında bir hata oluştu'
    });
  }
});

// -------------------------------------------------------------
// 5. POST /auth/login - Login with email and password
// (email, password -> id, fullName, email, token)
// -------------------------------------------------------------
app.post(['/auth/login', '/login', '/api/auth/login', '/api/login'], authRateLimiter, async (req, res) => {
  const { email, password } = req.body || {};

  if (typeof email !== 'string' || !email.trim()) {
    return res.status(400).json({
      success: false,
      error: 'Lütfen e-posta adresinizi giriniz'
    });
  }

  if (typeof password !== 'string' || !password) {
    return res.status(400).json({
      success: false,
      error: 'Lütfen şifrenizi giriniz'
    });
  }

  const cleanEmail = email.trim().toLowerCase();

  try {
    let user = null;

    if (pool) {
      const userRes = await pool.query(
        'SELECT id, full_name, email, password_hash FROM users WHERE email = $1',
        [cleanEmail]
      );
      if (userRes.rows.length > 0) {
        user = userRes.rows[0];
      }
    } else {
      user = memoryStore.userEmailMap.get(cleanEmail);
    }

    if (!user) {
      return res.status(401).json({
        success: false,
        error: 'E-posta veya şifre hatalı'
      });
    }

    const passwordMatch = await bcrypt.compare(password, user.password_hash);
    if (!passwordMatch) {
      return res.status(401).json({
        success: false,
        error: 'E-posta veya şifre hatalı'
      });
    }

    const token = jwt.sign(
      { userId: user.id, email: user.email },
      JWT_SECRET,
      { expiresIn: '30d' }
    );

    return res.status(200).json({
      success: true,
      message: 'Giriş başarılı',
      user: {
        id: user.id,
        fullName: user.full_name,
        email: user.email
      },
      token: token
    });
  } catch (error) {
    console.error('Error during /auth/login:', error.message);
    return res.status(500).json({
      success: false,
      error: 'Giriş işlemi sırasında bir hata oluştu'
    });
  }
});

// -------------------------------------------------------------
// 6. GET /auth/me - Validate token and retrieve user profile
// -------------------------------------------------------------
app.get('/auth/me', authenticateToken, async (req, res) => {
  const userId = req.user.userId;

  try {
    let user = null;
    if (pool) {
      const userRes = await pool.query(
        'SELECT id, full_name, email FROM users WHERE id = $1',
        [userId]
      );
      if (userRes.rows.length > 0) {
        user = userRes.rows[0];
      }
    } else {
      const memUser = memoryStore.users.get(userId);
      if (memUser) {
        user = {
          id: memUser.id,
          full_name: memUser.full_name,
          email: memUser.email
        };
      }
    }

    if (!user) {
      return res.status(404).json({
        success: false,
        error: 'Kullanıcı bulunamadı'
      });
    }

    return res.status(200).json({
      success: true,
      user: {
        id: user.id,
        fullName: user.full_name,
        email: user.email
      }
    });
  } catch (error) {
    console.error('Error in /auth/me:', error.message);
    return res.status(500).json({
      success: false,
      error: 'Kullanıcı bilgisi alınamadı'
    });
  }
});

// -------------------------------------------------------------
// 7. GET /save - Retrieve authenticated user's cloud save
// -------------------------------------------------------------
app.get('/save', authenticateToken, async (req, res) => {
  const userId = req.user.userId;

  try {
    if (pool) {
      const saveRes = await pool.query(
        'SELECT current_level, completed_levels, trophies, coins, lives, updated_at FROM game_saves WHERE user_id = $1',
        [userId]
      );

      if (saveRes.rows.length > 0) {
        const row = saveRes.rows[0];
        return res.status(200).json({
          success: true,
          save: {
            currentLevel: row.current_level,
            completedLevels: row.completed_levels || [],
            trophies: row.trophies,
            coins: row.coins,
            lives: row.lives,
            updatedAt: row.updated_at
          }
        });
      }

      // Default if no save row yet
      return res.status(200).json({
        success: true,
        save: {
          currentLevel: 1,
          completedLevels: [],
          trophies: 0,
          coins: 50,
          lives: 5,
          updatedAt: new Date().toISOString()
        }
      });
    }

    // Memory fallback
    const memSave = memoryStore.saves.get(userId) || {
      current_level: 1,
      completed_levels: [],
      trophies: 0,
      coins: 50,
      lives: 5,
      updated_at: new Date()
    };

    return res.status(200).json({
      success: true,
      save: {
        currentLevel: memSave.current_level,
        completedLevels: memSave.completed_levels,
        trophies: memSave.trophies,
        coins: memSave.coins,
        lives: memSave.lives,
        updatedAt: memSave.updated_at
      }
    });
  } catch (error) {
    console.error('Error fetching /save:', error.message);
    return res.status(500).json({
      success: false,
      error: 'Bulut kaydı alınamadı'
    });
  }
});

// -------------------------------------------------------------
// 8. POST /save - Update authenticated user's cloud save
// (Strict user isolation: Uses JWT userId, ignores client user_id)
// (Conflict resolution: Never regresses currentLevel, trophies, or completedLevels)
// -------------------------------------------------------------
app.post('/save', authenticateToken, async (req, res) => {
  const userId = req.user.userId; // Secure: derived exclusively from verified JWT
  const { currentLevel, completedLevels, trophies, coins, lives } = req.body || {};

  // Input validation
  const safeCurrentLevel = Number.isInteger(currentLevel) && currentLevel >= 1 ? currentLevel : 1;
  const safeCompletedLevels = Array.isArray(completedLevels)
    ? completedLevels.filter(lvl => Number.isInteger(lvl) && lvl >= 1)
    : [];
  const safeTrophies = Number.isInteger(trophies) && trophies >= 0 ? trophies : 0;
  const safeCoins = Number.isInteger(coins) && coins >= 0 ? coins : 0;
  const safeLives = Number.isInteger(lives) && lives >= 0 && lives <= 10 ? lives : 5;

  try {
    if (pool) {
      // Fetch existing save to intelligently merge progress
      const existingRes = await pool.query(
        'SELECT current_level, completed_levels, trophies, coins, lives FROM game_saves WHERE user_id = $1',
        [userId]
      );

      let mergedCurrentLevel = safeCurrentLevel;
      let mergedCompleted = safeCompletedLevels;
      let mergedTrophies = safeTrophies;
      let mergedCoins = safeCoins;
      let mergedLives = safeLives;

      if (existingRes.rows.length > 0) {
        const ex = existingRes.rows[0];
        mergedCurrentLevel = Math.max(ex.current_level || 1, safeCurrentLevel);
        mergedTrophies = Math.max(ex.trophies || 0, safeTrophies);
        mergedCoins = Math.max(ex.coins || 0, safeCoins);
        mergedLives = safeLives; // use current active lives

        // Safe set union of completed levels
        const existingArray = Array.isArray(ex.completed_levels) ? ex.completed_levels : [];
        mergedCompleted = Array.from(new Set([...existingArray, ...safeCompletedLevels])).sort((a, b) => a - b);

        const updateQuery = `
          UPDATE game_saves
          SET current_level = $1,
              completed_levels = $2,
              trophies = $3,
              coins = $4,
              lives = $5,
              updated_at = NOW()
          WHERE user_id = $6
          RETURNING current_level, completed_levels, trophies, coins, lives, updated_at;
        `;
        const updated = await pool.query(updateQuery, [
          mergedCurrentLevel,
          JSON.stringify(mergedCompleted),
          mergedTrophies,
          mergedCoins,
          mergedLives,
          userId
        ]);
        const row = updated.rows[0];

        return res.status(200).json({
          success: true,
          message: 'Bulut kaydı başarıyla güncellendi',
          save: {
            currentLevel: row.current_level,
            completedLevels: row.completed_levels,
            trophies: row.trophies,
            coins: row.coins,
            lives: row.lives,
            updatedAt: row.updated_at
          }
        });
      }

      // First time save insert
      const insertQuery = `
        INSERT INTO game_saves (user_id, current_level, completed_levels, trophies, coins, lives, updated_at)
        VALUES ($1, $2, $3, $4, $5, $6, NOW())
        RETURNING current_level, completed_levels, trophies, coins, lives, updated_at;
      `;
      const inserted = await pool.query(insertQuery, [
        userId,
        mergedCurrentLevel,
        JSON.stringify(mergedCompleted),
        mergedTrophies,
        mergedCoins,
        mergedLives
      ]);
      const row = inserted.rows[0];

      return res.status(200).json({
        success: true,
        message: 'Bulut kaydı başarıyla oluşturuldu',
        save: {
          currentLevel: row.current_level,
          completedLevels: row.completed_levels,
          trophies: row.trophies,
          coins: row.coins,
          lives: row.lives,
          updatedAt: row.updated_at
        }
      });
    }

    // Memory fallback
    const ex = memoryStore.saves.get(userId) || {
      current_level: 1,
      completed_levels: [],
      trophies: 0,
      coins: 50,
      lives: 5
    };

    const mergedCurrentLevel = Math.max(ex.current_level || 1, safeCurrentLevel);
    const mergedTrophies = Math.max(ex.trophies || 0, safeTrophies);
    const mergedCoins = Math.max(ex.coins || 0, safeCoins);
    const mergedCompleted = Array.from(new Set([...(ex.completed_levels || []), ...safeCompletedLevels])).sort((a, b) => a - b);

    const saved = {
      user_id: userId,
      current_level: mergedCurrentLevel,
      completed_levels: mergedCompleted,
      trophies: mergedTrophies,
      coins: mergedCoins,
      lives: safeLives,
      updated_at: new Date()
    };
    memoryStore.saves.set(userId, saved);

    return res.status(200).json({
      success: true,
      message: 'Bulut kaydı başarıyla güncellendi',
      save: {
        currentLevel: saved.current_level,
        completedLevels: saved.completed_levels,
        trophies: saved.trophies,
        coins: saved.coins,
        lives: saved.lives,
        updatedAt: saved.updated_at
      }
    });
  } catch (error) {
    console.error('Error saving in /save:', error.message);
    return res.status(500).json({
      success: false,
      error: 'Bulut kaydı kaydedilemedi'
    });
  }
});

// Fallback for unmatched routes
app.use((req, res) => {
  res.status(404).json({
    success: false,
    error: 'Endpoint not found'
  });
});

// Start listening if executed directly
if (require.main === module) {
  app.listen(PORT, HOST, () => {
    console.log(`ECHOFLUX Server listening on ${HOST}:${PORT}`);
  });
}

module.exports = app;
