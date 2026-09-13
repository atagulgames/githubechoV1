const express = require('express');
const cors = require('cors');
const { Pool } = require('pg');

const app = express();
const PORT = process.env.PORT || 3000;
const HOST = '0.0.0.0';

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
  console.warn('WARNING: DATABASE_URL environment variable is not defined.');
  console.warn('Health check (/health) will operate normally, but leaderboard database endpoints will return 503.');
}

// Automatically create leaderboard table and index if database is configured
async function initDatabase() {
  if (!pool) return;
  try {
    const ddlQuery = `
      CREATE TABLE IF NOT EXISTS leaderboard (
        id SERIAL PRIMARY KEY,
        username VARCHAR(20) UNIQUE NOT NULL,
        score INT NOT NULL DEFAULT 0,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      );
      CREATE INDEX IF NOT EXISTS idx_leaderboard_score ON leaderboard (score DESC);
    `;
    await pool.query(ddlQuery);
    console.log("Database initialized successfully: 'leaderboard' table is ready.");
  } catch (error) {
    console.error("Failed to initialize 'leaderboard' table:", error.message);
  }
}

initDatabase();

// 1. GET /health - Service health check
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'ok'
  });
});

// 2. GET /leaderboard - Fetch top 100 highest scores
app.get('/leaderboard', async (req, res) => {
  if (!pool) {
    return res.status(500).json({
      success: false,
      error: 'DATABASE_URL is not configured'
    });
  }

  try {
    const query = `
      SELECT username, score
      FROM leaderboard
      ORDER BY score DESC, updated_at ASC
      LIMIT 100
    `;
    const result = await pool.query(query);

    res.status(200).json({
      success: true,
      leaderboard: result.rows
    });
  } catch (error) {
    console.error('Error fetching leaderboard:', error.message);
    res.status(500).json({
      success: false,
      error: 'Failed to retrieve leaderboard from database'
    });
  }
});

// 3. POST /leaderboard - Submit or update player score
app.post('/leaderboard', async (req, res) => {
  const { username, score } = req.body || {};

  // Validate username
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

  // Reject malicious HTML / script tags
  const containsScriptOrHtml = /<[^>]*>|[<>]|javascript:/i.test(trimmedUsername);
  if (containsScriptOrHtml) {
    return res.status(400).json({
      success: false,
      error: 'Invalid username: HTML tags or script content are not allowed'
    });
  }

  // Validate score
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

  // Maximum signed 32-bit integer supported by PostgreSQL INT
  if (score > 2147483647) {
    return res.status(400).json({
      success: false,
      error: 'Invalid score: Value exceeds maximum allowed limit'
    });
  }

  if (!pool) {
    return res.status(500).json({
      success: false,
      error: 'DATABASE_URL is not configured'
    });
  }

  try {
    // If username already exists:
    // Update score ONLY IF new score > existing score.
    // Update updated_at ONLY IF new score was higher.
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

    res.status(200).json({
      success: true,
      message: 'Score recorded successfully',
      entry: {
        username: savedEntry.username,
        score: savedEntry.score
      }
    });
  } catch (error) {
    console.error('Error saving score to database:', error.message);
    res.status(500).json({
      success: false,
      error: 'Database error while saving score'
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

// Start listening
app.listen(PORT, HOST, () => {
  console.log(`ECHOFLUX Leaderboard Server listening on ${HOST}:${PORT}`);
});
