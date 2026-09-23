const express = require('express');
const router = express.Router();
const path = require('path');
const fs = require('fs');
const Faculty = require('../models/Faculty');
const BirthdayLog = require('../models/BirthdayLog');
const { generateCard } = require('../services/canvasService');
const { runDailyBirthdayWorkflow } = require('../services/cronService');

/**
 * GET /api/birthdays/today
 * Filter staff whose DOB (DD-MM) matches today's date.
 */
router.get('/today', async (req, res) => {
  try {
    const now = new Date();
    const todayDay = String(now.getDate()).padStart(2, '0');
    const todayMonth = String(now.getMonth() + 1).padStart(2, '0');
    const todayDayMonth = `${todayDay}-${todayMonth}`;

    const allFaculty = await Faculty.find({});
    const todayStaff = allFaculty.filter(f => {
      if (!f.dob) return false;
      const parts = f.dob.split('-');
      if (parts.length < 2) return false;
      const dm = `${parts[0].padStart(2, '0')}-${parts[1].padStart(2, '0')}`;
      return dm === todayDayMonth;
    });

    res.json({
      success: true,
      date: todayDayMonth,
      count: todayStaff.length,
      data: todayStaff
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * GET /api/birthdays/upcoming
 * Retrieve staff with birthdays in the next 7 days.
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
      let nextBday = new Date(currentYear, month, day);

      // Reset time to start of day for accurate comparison
      const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      nextBday.setHours(0, 0, 0, 0);

      // If already passed this year, look at next year
      if (nextBday < todayStart) {
        nextBday = new Date(currentYear + 1, month, day);
      }

      const diffTime = nextBday.getTime() - todayStart.getTime();
      const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));

      if (diffDays >= 0 && diffDays <= daysAhead) {
        upcoming.push({
          faculty,
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
 * Dynamically generates and returns the 1200x675 HD Card PNG for a faculty member.
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
      return res.status(404).json({ success: false, error: 'Faculty not found' });
    }

    const previewDir = path.join(__dirname, '../uploads/previews');
    if (!fs.existsSync(previewDir)) fs.mkdirSync(previewDir, { recursive: true });

    const previewFilePath = path.join(previewDir, `preview_${faculty.facultyId}.png`);
    await generateCard(faculty, previewFilePath);

    res.sendFile(previewFilePath);
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * POST /api/birthdays/trigger-now
 * Manually trigger the daily workflow (for testing / admin bypass).
 */
router.post('/trigger-now', async (req, res) => {
  try {
    const { targetDate } = req.body; // optional "DD-MM"
    const result = await runDailyBirthdayWorkflow('MANUAL_TRIGGER', targetDate);
    res.json(result);
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
