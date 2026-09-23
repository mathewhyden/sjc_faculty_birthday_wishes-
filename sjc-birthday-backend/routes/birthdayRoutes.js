const express = require('express');
const router = express.Router();
const path = require('path');
const fs = require('fs');
const Faculty = require('../models/Faculty');
const BirthdayLog = require('../models/BirthdayLog');
const { generateCard } = require('../services/canvasService');
const { sendBirthdayGreeting } = require('../services/whatsappService');
const { runDailyBirthdayWorkflow, getTodayDateStrings } = require('../services/cronService');
const { getDepartmentFullName } = require('../services/deptMapper');

/**
 * GET /api/birthdays/today
 * Filters all staff whose birthday (DD-MM) matches today's date in Asia/Kolkata timezone.
 */
router.get('/today', async (req, res) => {
  try {
    const { dayMonth, fullDate } = getTodayDateStrings();

    const allFaculty = await Faculty.find({});
    const todayStaff = allFaculty.filter(staff => {
      if (!staff.dob) return false;
      const parts = staff.dob.split('-');
      if (parts.length < 2) return false;
      const dm = `${parts[0].padStart(2, '0')}-${parts[1].padStart(2, '0')}`;
      return dm === dayMonth;
    });

    const enriched = todayStaff.map(f => ({
      ...f.toObject(),
      fullDeptName: getDepartmentFullName(f.deptCode)
    }));

    res.json({
      success: true,
      currentDate: fullDate,
      dayMonth,
      count: enriched.length,
      data: enriched
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * GET /api/birthdays/upcoming
 * 7-Day Birthday Radar: Computes exact remaining days for all staff.
 */
router.get('/upcoming', async (req, res) => {
  try {
    const daysAhead = parseInt(req.query.days, 10) || 7;
    const now = new Date();
    const currentYear = now.getFullYear();

    const allFaculty = await Faculty.find({});
    const upcoming = [];

    allFaculty.forEach(faculty => {
      if (!faculty.dob) return;
      const parts = faculty.dob.split('-');
      if (parts.length < 2) return;

      const day = parseInt(parts[0], 10);
      const month = parseInt(parts[1], 10) - 1; // 0-indexed month

      // Create target date for this year
      let targetBday = new Date(currentYear, month, day);
      const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      targetBday.setHours(0, 0, 0, 0);

      // If already passed in current year, calculate for next year
      if (targetBday < todayStart) {
        targetBday = new Date(currentYear + 1, month, day);
      }

      const diffMs = targetBday.getTime() - todayStart.getTime();
      const diffDays = Math.round(diffMs / (1000 * 60 * 60 * 24));

      if (diffDays >= 0 && diffDays <= daysAhead) {
        upcoming.push({
          faculty: {
            ...faculty.toObject(),
            fullDeptName: getDepartmentFullName(faculty.deptCode)
          },
          daysRemaining: diffDays,
          birthdayDate: `${String(day).padStart(2, '0')}-${String(month + 1).padStart(2, '0')}`
        });
      }
    });

    upcoming.sort((a, b) => a.daysRemaining - b.daysRemaining);

    res.json({
      success: true,
      windowDays: daysAhead,
      count: upcoming.length,
      data: upcoming
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * GET /api/birthdays/preview/:id
 * Dynamically generates and streams the 1200x675 HD PNG gift card for instant viewing.
 */
router.get('/preview/:id', async (req, res) => {
  try {
    const id = req.params.id;
    let faculty = null;

    if (id.match(/^[0-9a-fA-F]{24}$/)) {
      faculty = await Faculty.findById(id);
    }
    if (!faculty) {
      faculty = await Faculty.findOne({ facultyId: id });
    }

    if (!faculty) {
      return res.status(404).json({ success: false, error: 'Faculty member not found' });
    }

    const previewDir = path.resolve(__dirname, '../uploads/cards');
    if (!fs.existsSync(previewDir)) fs.mkdirSync(previewDir, { recursive: true });

    const previewFileName = `preview_${faculty.facultyId}.png`;
    const previewFilePath = path.join(previewDir, previewFileName);

    await generateCard(faculty, previewFilePath);

    res.setHeader('Content-Type', 'image/png');
    res.sendFile(previewFilePath);
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * POST /api/birthdays/trigger-now
 * Manually executes the daily birthday workflow (ideal for admin testing and on-demand runs).
 * Body: { targetDate?: "DD-MM" }
 */
router.post('/trigger-now', async (req, res) => {
  try {
    const { targetDate } = req.body;
    console.log(`[Admin API] Manual birthday trigger received. Target: ${targetDate || 'Today'}`);

    const result = await runDailyBirthdayWorkflow('MANUAL_TRIGGER', targetDate);
    res.json(result);
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * POST /api/birthdays/send-single
 * Sends an official WhatsApp greeting & gift card to a specific faculty member on demand.
 * Body: { facultyId: "...", targetNumber?: "..." }
 */
router.post('/send-single', async (req, res) => {
  try {
    const { facultyId, targetNumber } = req.body;
    if (!facultyId) {
      return res.status(400).json({ success: false, error: 'facultyId is required' });
    }

    const faculty = await Faculty.findOne({
      $or: [{ facultyId }, { _id: facultyId.match(/^[0-9a-fA-F]{24}$/) ? facultyId : null }]
    });

    if (!faculty) {
      return res.status(404).json({ success: false, error: 'Faculty member not found' });
    }

    if (targetNumber) {
      faculty.mobile = targetNumber;
    }

    const { fullDate } = getTodayDateStrings();
    const cardsDir = path.resolve(__dirname, '../uploads/cards');
    if (!fs.existsSync(cardsDir)) fs.mkdirSync(cardsDir, { recursive: true });

    const cardFileName = `card_${faculty.facultyId}_admin_${Date.now()}.png`;
    const cardFilePath = path.join(cardsDir, cardFileName);
    const cardPublicUrl = `/uploads/cards/${cardFileName}`;

    await generateCard(faculty, cardFilePath);
    const dispatchRes = await sendBirthdayGreeting(faculty, cardFilePath);

    const log = await BirthdayLog.create({
      runDate: fullDate,
      triggerType: 'ADMIN_SINGLE_SEND',
      facultyId: faculty.facultyId,
      facultyName: faculty.name,
      deptCode: faculty.deptCode,
      fullDeptName: getDepartmentFullName(faculty.deptCode),
      mobile: faculty.mobile,
      recipientMobile: dispatchRes.recipientNumber || faculty.mobile,
      isTestMode: dispatchRes.isTestMode || false,
      cardImagePath: cardFilePath,
      cardPublicUrl,
      messageText: dispatchRes.messageText || '',
      whatsappStatus: dispatchRes.success ? 'SUCCESS' : 'FAILED',
      whatsappMessageId: dispatchRes.messageId || '',
      responsePayload: dispatchRes.metaResponse || null,
      errorMessage: dispatchRes.error || ''
    });

    res.json({
      success: dispatchRes.success,
      message: `Greeting dispatched to ${faculty.name} (${faculty.mobile})`,
      dispatch: dispatchRes,
      log
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * GET /api/birthdays/logs
 * Query recent WhatsApp dispatch audit logs.
 */
router.get('/logs', async (req, res) => {
  try {
    const limit = Math.min(100, parseInt(req.query.limit, 10) || 50);
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
