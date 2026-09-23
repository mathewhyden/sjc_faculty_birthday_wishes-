require('dotenv').config();
const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');

const connectDB = require('./config/db');
const { initCronScheduler } = require('./services/cronService');

const facultyRoutes = require('./routes/facultyRoutes');
const birthdayRoutes = require('./routes/birthdayRoutes');
const whatsappRoutes = require('./routes/whatsappRoutes');

const app = express();
const PORT = process.env.PORT || 5000;

// Ensure upload directories exist
const uploadDirs = [
  path.join(__dirname, 'uploads'),
  path.join(__dirname, 'uploads/faculty_photos'),
  path.join(__dirname, 'uploads/generated_cards'),
  path.join(__dirname, 'uploads/previews')
];
uploadDirs.forEach(dir => {
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
});

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Static file serving: Uploaded Photos & Generated Cards
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Static file serving: Admin Dashboard Web Interface
app.use(express.static(path.join(__dirname, 'public')));

// API Routes
app.use('/api/faculty', facultyRoutes);
app.use('/api/birthdays', birthdayRoutes);
app.use('/api/whatsapp', whatsappRoutes);

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({
    status: 'ONLINE',
    system: 'St. Joseph\'s College Birthday Wishes Automation Backend',
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});

// Admin Web Interface Route
app.get('/admin', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Connect to Database and Start Server
async function startServer() {
  // 1. Connect MongoDB
  await connectDB();

  // 2. Initialize Automated Midnight Cron Scheduler
  initCronScheduler();

  // 3. Start Express HTTP Server
  const server = app.listen(PORT, () => {
    console.log(`\n============================================================`);
    console.log(`🎓 ST. JOSEPH'S COLLEGE (AUTONOMOUS) - BIRTHDAY SERVER`);
    console.log(`🚀 Server listening on port: ${PORT}`);
    console.log(`🔗 API Base: http://localhost:${PORT}/api`);
    console.log(`🌐 Admin Dashboard: http://localhost:${PORT}/admin`);
    console.log(`============================================================\n`);
  });

  // Graceful shutdown handling
  const shutdown = () => {
    console.log('\n[Server] Gracefully shutting down...');
    server.close(() => {
      console.log('[Server] HTTP server closed.');
      process.exit(0);
    });
  };

  process.on('SIGTERM', shutdown);
  process.on('SIGINT', shutdown);
}

startServer();

module.exports = app;
