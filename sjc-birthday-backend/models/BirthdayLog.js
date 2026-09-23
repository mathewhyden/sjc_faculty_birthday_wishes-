const mongoose = require('mongoose');

const BirthdayLogSchema = new mongoose.Schema(
  {
    runDate: {
      type: String,
      required: true,
      index: true // e.g. "23-09-2026"
    },
    triggerType: {
      type: String,
      enum: ['AUTOMATED_CRON_8AM', 'MANUAL_TRIGGER', 'ADMIN_SINGLE_SEND', 'TEST_DISPATCH'],
      default: 'AUTOMATED_CRON_8AM',
      index: true
    },
    facultyId: {
      type: String,
      required: true,
      index: true
    },
    facultyName: {
      type: String,
      required: true
    },
    deptCode: {
      type: String,
      required: true
    },
    fullDeptName: {
      type: String
    },
    mobile: {
      type: String,
      required: true
    },
    recipientMobile: {
      type: String
    },
    isTestMode: {
      type: Boolean,
      default: false
    },
    cardImagePath: {
      type: String
    },
    cardPublicUrl: {
      type: String
    },
    messageText: {
      type: String
    },
    whatsappStatus: {
      type: String,
      enum: ['SUCCESS', 'FAILED', 'QUEUED', 'SKIPPED'],
      default: 'QUEUED',
      index: true
    },
    whatsappMessageId: {
      type: String,
      default: ''
    },
    responsePayload: {
      type: mongoose.Schema.Types.Mixed
    },
    errorMessage: {
      type: String,
      default: ''
    }
  },
  {
    timestamps: true
  }
);

module.exports = mongoose.model('BirthdayLog', BirthdayLogSchema);
