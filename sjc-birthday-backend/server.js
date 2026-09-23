require('dotenv').config();
const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const mongoose = require('mongoose');

const facultyRoutes = require('./routes/facultyRoutes');
const birthdayRoutes = require('./routes/birthdayRoutes');
const { initDailyCronScheduler } = require('./services/cronService');

const app = express();
const PORT = process.env.PORT || 5000;
const HOST = process.env.HOST || '0.0.0.0';

// Ensure necessary upload folders exist
const requiredDirs = [
  path.resolve(__dirname, 'uploads'),
  path.resolve(__dirname, 'uploads/faculty_photos'),
  path.resolve(__dirname, 'uploads/cards'),
  path.resolve(__dirname, 'public')
];
requiredDirs.forEach(dir => {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
});

// Middlewares
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));
app.use(express.json({ limit: '15mb' }));
app.use(express.urlencoded({ extended: true, limit: '15mb' }));

// Static file hosting for faculty photos & generated cards
app.use('/uploads', express.static(path.resolve(__dirname, 'uploads')));
app.use(express.static(path.resolve(__dirname, 'public')));

// API Routes
app.use('/api/faculty', facultyRoutes);
app.use('/api/birthdays', birthdayRoutes);

// Health check endpoint (Render / Railway / Docker health probes)
app.get('/api/health', (req, res) => {
  const dbState = mongoose.connection.readyState;
  const dbStatusMap = {
    0: 'DISCONNECTED',
    1: 'CONNECTED',
    2: 'CONNECTING',
    3: 'DISCONNECTING'
  };

  res.json({
    status: 'ONLINE',
    service: 'St. Joseph\'s College Birthday Wishes Automation Engine',
    port: PORT,
    host: HOST,
    nodeEnv: process.env.NODE_ENV || 'production',
    cronSchedule: process.env.CRON_SCHEDULE || '0 0 8 * * * (Daily 8:00 AM IST)',
    cronTimezone: process.env.CRON_TIMEZONE || 'Asia/Kolkata',
    database: {
      status: dbStatusMap[dbState] || 'UNKNOWN',
      name: mongoose.connection.name || 'sjc_birthday_db'
    },
    testMode: String(process.env.TEST_MODE).toLowerCase() === 'true',
    uptimeSeconds: Math.floor(process.uptime()),
    timestamp: new Date().toISOString()
  });
});

// Admin Dashboard route
app.get('/admin', (req, res) => {
  const adminIndex = path.resolve(__dirname, 'public/index.html');
  if (fs.existsSync(adminIndex)) {
    res.sendFile(adminIndex);
  } else {
    res.send('<h2>SJC Birthday Backend Admin Interface</h2><p>API is active at /api</p>');
  }
});

// 404 Route handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    error: `Cannot ${req.method} ${req.originalUrl}`
  });
});

// Global Error Handler
app.use((err, req, res, next) => {
  console.error('[Server Error]', err.stack || err.message);
  res.status(500).json({
    success: false,
    error: err.message || 'Internal Server Error'
  });
});

// Database Connection & Server Bootstrap
async function startServer() {
  const mongoUri =
    process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017/sjc_birthday_db';

  console.log(`\n========================================================================`);
  console.log(`🎓 ST. JOSEPH'S COLLEGE (AUTONOMOUS) - 24/7 CLOUD BACKEND ENGINE`);
  console.log(`========================================================================`);
  console.log(`📡 Initializing MongoDB Connection...`);

  try {
    await mongoose.connect(mongoUri, {
      serverSelectionTimeoutMS: 5000
    });
    console.log(`✅ MongoDB Atlas Connected! Database: "${mongoose.connection.name}"`);
  } catch (dbErr) {
    console.warn(`⚠️ Warning: MongoDB connection failed (${dbErr.message}).`);
    console.warn(`   Server will continue running; ensure MONGODB_URI is configured in .env`);
  }

  // Initialize automated daily 8:00 AM IST cron job
  initDailyCronScheduler();

  // Listen on all network interfaces (0.0.0.0) for cloud compatibility
  const server = app.listen(PORT, HOST, () => {
    console.log(`\n🚀 SJC Backend Server LIVE & READY!`);
    console.log(`   • Host / Port: http://${HOST}:${PORT}`);
    console.log(`   • Health Probe: http://${HOST}:${PORT}/api/health`);
    console.log(`   • Admin Hub:    http://${HOST}:${PORT}/admin`);
    console.log(`   • Staff API:    http://${HOST}:${PORT}/api/faculty`);
    console.log(`   • Birthday API: http://${HOST}:${PORT}/api/birthdays/today`);
    console.log(`========================================================================\n`);
  });

  // Graceful shutdown
  const gracefulShutdown = signal => {
    console.log(`\n[Server] Received ${signal}. Shutting down gracefully...`);
    server.close(async () => {
      console.log('[Server] HTTP listener closed.');
      try {
        await mongoose.connection.close();
        console.log('[Server] MongoDB connection closed.');
      } catch (e) {}
      process.exit(0);
    });
  };

  process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
  process.on('SIGINT', () => gracefulShutdown('SIGINT'));
}

startServer();

module.exports = app;
