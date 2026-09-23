const mongoose = require('mongoose');

const BirthdayLogSchema = new mongoose.Schema({
  runDate: {
    type: String,
    required: true,
    index: true // e.g. "23-09-2026"
  },
  triggerType: {
    type: String,
    enum: ['AUTOMATED_CRON', 'MANUAL_TRIGGER', 'ADMIN_TEST'],
    default: 'AUTOMATED_CRON'
  },
  facultyId: {
    type: String,
    required: true
  },
  facultyName: {
    type: String,
    required: true
  },
  deptCode: {
    type: String,
    required: true
  },
  mobile: {
    type: String,
    required: true
  },
  cardImagePath: {
    type: String,
    default: ''
  },
  whatsappStatus: {
    type: String,
    enum: ['SUCCESS', 'FAILED', 'PENDING', 'SIMULATED'],
    default: 'PENDING'
  },
  whatsappMessageId: {
    type: String,
    default: ''
  },
  errorMessage: {
    type: String,
    default: ''
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('BirthdayLog', BirthdayLogSchema);
