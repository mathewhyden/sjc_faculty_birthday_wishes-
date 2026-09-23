const mongoose = require('mongoose');

const connectDB = async () => {
  try {
    const conn = await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/sjc_birthday_db', {
      serverSelectionTimeoutMS: 5000,
      autoIndex: true
    });
    console.log(`[MongoDB] Connected successfully: ${conn.connection.host} (DB: ${conn.connection.name})`);
  } catch (err) {
    console.error(`[MongoDB Connection Error] ${err.message}`);
    console.log('[MongoDB] Running in offline / memory fallback mode if Atlas URI is pending.');
  }
};

mongoose.connection.on('disconnected', () => {
  console.warn('[MongoDB] Disconnected. Attempting reconnection...');
});

mongoose.connection.on('error', (err) => {
  console.error('[MongoDB Error]', err.message);
});

module.exports = connectDB;
