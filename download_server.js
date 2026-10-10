const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const HOST = '0.0.0.0';
const ZIP_FILE_PATH = path.join(__dirname, 'project_source.zip');

function serveZip(req, res) {
  if (!fs.existsSync(ZIP_FILE_PATH)) {
    res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
    return res.end('ZIP dosyası bulunamadı.');
  }

  const stat = fs.statSync(ZIP_FILE_PATH);
  const fileSize = stat.size;
  const range = req.headers.range;

  if (range) {
    const parts = range.replace(/bytes=/, '').split('-');
    const start = parseInt(parts[0], 10);
    const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
    const chunksize = (end - start) + 1;
    const file = fs.createReadStream(ZIP_FILE_PATH, { start, end });
    const head = {
      'Content-Range': `bytes ${start}-${end}/${fileSize}`,
      'Accept-Ranges': 'bytes',
      'Content-Length': chunksize,
      'Content-Type': 'application/zip',
      'Content-Disposition': 'attachment; filename="project_source.zip"'
    };
    res.writeHead(206, head);
    file.pipe(res);
  } else {
    const head = {
      'Content-Length': fileSize,
      'Content-Type': 'application/zip',
      'Content-Disposition': 'attachment; filename="project_source.zip"',
      'Accept-Ranges': 'bytes'
    };
    res.writeHead(200, head);
    fs.createReadStream(ZIP_FILE_PATH).pipe(res);
  }
}

const server = http.createServer((req, res) => {
  const urlPath = req.url.split('?')[0];

  if (urlPath === '/project_source.zip' || urlPath === '/download' || urlPath === '/echo_source.zip' || urlPath === '/echo.zip') {
    return serveZip(req, res);
  }

  if (urlPath === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    return res.end(JSON.stringify({ status: 'ok', file: fs.existsSync(ZIP_FILE_PATH) }));
  }

  // Beautiful Landing & One-Click Download Page for Root URL
  let fileSizeMb = '20 MB';
  try {
    if (fs.existsSync(ZIP_FILE_PATH)) {
      fileSizeMb = (fs.statSync(ZIP_FILE_PATH).size / (1024 * 1024)).toFixed(1) + ' MB';
    }
  } catch (e) {}

  const html = `<!DOCTYPE html>
<html lang="tr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>ECHOFLUX - Proje Kaynak Kodları (ZIP İndir)</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
      background: radial-gradient(circle at top, #1e293b, #0f172a 80%);
      color: #f8fafc;
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1.5rem;
    }
    .card {
      background: rgba(30, 41, 59, 0.85);
      backdrop-filter: blur(12px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 20px;
      padding: 2.5rem;
      max-width: 580px;
      width: 100%;
      box-shadow: 0 20px 40px rgba(0,0,0,0.5);
      text-align: center;
    }
    .badge {
      display: inline-block;
      background: rgba(56, 189, 248, 0.15);
      color: #38bdf8;
      border: 1px solid rgba(56, 189, 248, 0.3);
      padding: 6px 14px;
      border-radius: 999px;
      font-size: 13px;
      font-weight: 600;
      margin-bottom: 1.25rem;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    h1 {
      font-size: 1.85rem;
      font-weight: 800;
      margin-bottom: 0.75rem;
      background: linear-gradient(135deg, #ffffff 30%, #94a3b8);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    p {
      color: #94a3b8;
      font-size: 15px;
      line-height: 1.6;
      margin-bottom: 1.75rem;
    }
    .btn-download {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      background: linear-gradient(135deg, #0284c7, #2563eb);
      color: #ffffff;
      padding: 16px 32px;
      border-radius: 12px;
      font-size: 17px;
      font-weight: 700;
      text-decoration: none;
      box-shadow: 0 10px 20px rgba(37, 99, 235, 0.35);
      transition: all 0.2s ease;
      cursor: pointer;
      border: none;
      width: 100%;
    }
    .btn-download:hover {
      background: linear-gradient(135deg, #0369a1, #1d4ed8);
      transform: translateY(-2px);
      box-shadow: 0 14px 26px rgba(37, 99, 235, 0.45);
    }
    .btn-download:active {
      transform: translateY(0);
    }
    .features {
      margin-top: 2rem;
      padding-top: 1.5rem;
      border-top: 1px solid rgba(255, 255, 255, 0.08);
      text-align: left;
    }
    .features h3 {
      font-size: 13px;
      text-transform: uppercase;
      letter-spacing: 0.8px;
      color: #cbd5e1;
      margin-bottom: 0.75rem;
    }
    .features ul {
      list-style: none;
      font-size: 14px;
      color: #94a3b8;
      line-height: 1.8;
    }
    .features li::before {
      content: "✓ ";
      color: #38bdf8;
      font-weight: bold;
    }
    .footer-note {
      margin-top: 1.5rem;
      font-size: 12px;
      color: #64748b;
    }
  </style>
</head>
<body>
  <div class="card">
    <div class="badge">Codex / Android Projesi</div>
    <h1>ECHOFLUX Kaynak Kod Arşivi</h1>
    <p>Codex'e doğrudan aktarabileceğiniz tüm Kotlin, Jetpack Compose, ses dosyaları, görseller ve Gradle yapılandırmalarını içeren ZIP arşivi hazırlandı.</p>
    
    <a href="/project_source.zip" class="btn-download" download="project_source.zip">
      <span>📥</span>
      <span>project_source.zip İndir (${fileSizeMb})</span>
    </a>

    <div class="features">
      <h3>Arşiv İçeriği:</h3>
      <ul>
        <li>Tüm Kotlin & Jetpack Compose ekranları ve oyun motoru</li>
        <li>Kayıt zorunluluğu ve kimlik doğrulama sistemleri</li>
        <li>Tüm görsel, ses (MP3) ve video varlıkları</li>
        <li>Gradle, Manifest ve tam proje yapılandırma dosyaları</li>
      </ul>
    </div>

    <div class="footer-note">
      Tıkladığınızda dosya doğrudan tarayıcınız tarafından indirilecektir.
    </div>
  </div>
</body>
</html>`;

  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(html);
});

server.listen(PORT, HOST, () => {
  console.log(`Download server running at http://${HOST}:${PORT}`);
});
