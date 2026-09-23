const express = require('express');
const router = express.Router();
const path = require('path');
const Faculty = require('../models/Faculty');
const BirthdayLog = require('../models/BirthdayLog');
const { generateCard } = require('../services/canvasService');
const { sendBirthdayGreeting, buildGreetingMessage } = require('../services/whatsappService');
const { getTodayDateStrings } = require('../services/cronService');

/**
 * POST /api/whatsapp/send
 * Trigger manual or automated WhatsApp greeting message + card PNG.
 * Body: { facultyId: "...", targetNumber: "..." (optional override) }
 */
router.post('/send', async (req, res) => {
  try {
    const { facultyId, targetNumber } = req.body;

    if (!facultyId) {
      return res.status(400).json({ success: false, error: 'facultyId is required' });
    }

    const faculty = await Faculty.findOne({
      $or: [{ facultyId: facultyId }, { _id: facultyId.match(/^[0-9a-fA-F]{24}$/) ? facultyId : null }]
    });

    if (!faculty) {
      return res.status(404).json({ success: false, error: 'Faculty member not found' });
    }

    // Optional override for testing
    if (targetNumber) {
      faculty.mobile = targetNumber;
    }

    const { fullDate } = getTodayDateStrings();
    const cardsDir = path.join(__dirname, '../uploads/generated_cards');
    const cardFileName = `card_${faculty.facultyId}_manual_${Date.now()}.png`;
    const cardFilePath = path.join(cardsDir, cardFileName);

    // 1. Generate Card
    await generateCard(faculty, cardFilePath);
    const publicCardPath = `/uploads/generated_cards/${cardFileName}`;

    // 2. Dispatch
    const dispatchResult = await sendBirthdayGreeting(faculty, cardFilePath);

    // 3. Log
    const log = await BirthdayLog.create({
      runDate: fullDate,
      triggerType: 'ADMIN_TEST',
      facultyId: faculty.facultyId,
      facultyName: faculty.name,
      deptCode: faculty.deptCode,
      mobile: faculty.mobile,
      cardImagePath: publicCardPath,
      whatsappStatus: dispatchResult.success ? 'SUCCESS' : 'FAILED',
      whatsappMessageId: dispatchResult.messageId || '',
      errorMessage: dispatchResult.error || ''
    });

    res.json({
      success: dispatchResult.success,
      message: `Greeting sent to ${faculty.name} (${faculty.mobile})`,
      cardUrl: publicCardPath,
      messageText: buildGreetingMessage(faculty),
      log
    });

  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * GET /api/whatsapp/logs
 * Retrieve recent dispatch logs.
 */
router.get('/logs', async (req, res) => {
  try {
    const limit = parseInt(req.query.limit, 10) || 50;
    const logs = await BirthdayLog.find({}).sort({ createdAt: -1 }).limit(limit);
    res.json({
      success: true,
      count: logs.length,
      data: logs
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
