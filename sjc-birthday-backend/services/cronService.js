const cron = require('node-cron');
const path = require('path');
const fs = require('fs');
const Faculty = require('../models/Faculty');
const BirthdayLog = require('../models/BirthdayLog');
const { generateCard } = require('./canvasService');
const { sendBirthdayGreeting } = require('./whatsappService');
const { getDepartmentFullName } = require('./deptMapper');

/**
 * Returns formatted date strings for today in Asia/Kolkata timezone.
 */
function getTodayDateStrings() {
  const options = { timeZone: 'Asia/Kolkata' };
  const formatter = new Intl.DateTimeFormat('en-GB', {
    ...options,
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });

  // en-GB produces DD/MM/YYYY
  const parts = formatter.format(new Date()).split('/');
  const day = parts[0];
  const month = parts[1];
  const year = parts[2];

  return {
    dayMonth: `${day}-${month}`,
    fullDate: `${day}-${month}-${year}`
  };
}

/**
 * Core Daily Workflow:
 * 1. Queries MongoDB for today's birthday staff.
 * 2. Resolves department name.
 * 3. Renders dynamic 1200x675 HD PNG gift card.
 * 4. Dispatches WhatsApp message with attached card.
 * 5. Logs result in MongoDB BirthdayLog collection.
 *
 * @param {string} triggerType - 'AUTOMATED_CRON_8AM' | 'MANUAL_TRIGGER'
 * @param {string} targetDayMonth - Optional override in 'DD-MM' format
 */
async function runDailyBirthdayWorkflow(triggerType = 'AUTOMATED_CRON_8AM', targetDayMonth = null) {
  const { dayMonth, fullDate } = getTodayDateStrings();
  const searchDayMonth = targetDayMonth || dayMonth;

  console.log(`\n========================================================================`);
  console.log(`[Cron Workflow] 🎂 Starting SJC Birthday Processing for: ${searchDayMonth}`);
  console.log(`[Cron Workflow] Trigger Type: ${triggerType} | Run Date: ${fullDate}`);
  console.log(`========================================================================`);

  const results = [];

  try {
    // 1. Query all faculty members from MongoDB
    const allFaculty = await Faculty.find({});
    console.log(`[Cron Workflow] Total faculty in database: ${allFaculty.length}`);

    // Filter staff whose DOB matches target DD-MM
    const todayBirthdays = allFaculty.filter(staff => {
      if (!staff.dob) return false;
      const parts = staff.dob.split('-');
      if (parts.length < 2) return false;
      const staffDM = `${parts[0].padStart(2, '0')}-${parts[1].padStart(2, '0')}`;
      return staffDM === searchDayMonth;
    });

    console.log(`[Cron Workflow] 🎯 Found ${todayBirthdays.length} birthday celebrant(s) for ${searchDayMonth}.`);

    if (todayBirthdays.length === 0) {
      console.log(`[Cron Workflow] No birthdays today. Process completed.`);
      return {
        success: true,
        runDate: fullDate,
        dayMonth: searchDayMonth,
        processedCount: 0,
        birthdays: [],
        results: []
      };
    }

    // Storage directory for generated cards
    const cardsDir = path.resolve(__dirname, '../uploads/cards');
    if (!fs.existsSync(cardsDir)) {
      fs.mkdirSync(cardsDir, { recursive: true });
    }

    // 2. Process each faculty celebrant
    for (const faculty of todayBirthdays) {
      const fullDept = getDepartmentFullName(faculty.deptCode);
      console.log(`\n-> Celebrant: ${faculty.name} | Dept: ${faculty.deptCode} (${fullDept}) | Mobile: ${faculty.mobile}`);

      const cleanFacultyId = faculty.facultyId.replace(/[^a-zA-Z0-9_-]/g, '');
      const cardFileName = `card_${cleanFacultyId}_${fullDate.replace(/-/g, '_')}_${Date.now()}.png`;
      const cardFilePath = path.join(cardsDir, cardFileName);
      const relativeCardUrl = `/uploads/cards/${cardFileName}`;

      let whatsappStatus = 'FAILED';
      let messageId = '';
      let errorMessage = '';
      let dispatchRes = null;

      try {
        // 3. Generate 1200x675 HD PNG gift card
        console.log(`   [Card Engine] Rendering 1200x675 HD gift card for ${faculty.name}...`);
        await generateCard(faculty, cardFilePath);
        console.log(`   [Card Engine] Card generated at: ${cardFilePath}`);

        // 4. Dispatch WhatsApp message with attached PNG card
        console.log(`   [WhatsApp] Dispatching via Meta Cloud API...`);
        dispatchRes = await sendBirthdayGreeting(faculty, cardFilePath);

        if (dispatchRes.success) {
          whatsappStatus = 'SUCCESS';
          messageId = dispatchRes.messageId || '';
          console.log(`   [WhatsApp] ✅ Delivery successful! (ID: ${messageId})`);
        } else {
          whatsappStatus = 'FAILED';
          errorMessage = dispatchRes.error || 'WhatsApp delivery failure';
          console.warn(`   [WhatsApp] ⚠️ Delivery failed: ${errorMessage}`);
        }
      } catch (err) {
        console.error(`   [Error] Processing failed for ${faculty.name}:`, err.message);
        whatsappStatus = 'FAILED';
        errorMessage = err.message;
      }

      // 5. Save log record in MongoDB BirthdayLog collection
      try {
        const logEntry = await BirthdayLog.create({
          runDate: fullDate,
          triggerType,
          facultyId: faculty.facultyId,
          facultyName: faculty.name,
          deptCode: faculty.deptCode,
          fullDeptName: fullDept,
          mobile: faculty.mobile,
          recipientMobile: dispatchRes?.recipientNumber || faculty.mobile,
          isTestMode: dispatchRes?.isTestMode || false,
          cardImagePath: cardFilePath,
          cardPublicUrl: relativeCardUrl,
          messageText: dispatchRes?.messageText || '',
          whatsappStatus,
          whatsappMessageId: messageId,
          responsePayload: dispatchRes?.metaResponse || null,
          errorMessage
        });

        results.push(logEntry);
      } catch (logErr) {
        console.error(`   [Log Error] Failed to persist BirthdayLog to MongoDB:`, logErr.message);
      }
    }

    console.log(`\n========================================================================`);
    console.log(`[Cron Workflow] ✅ Completed! Successfully logged ${results.length} record(s).`);
    console.log(`========================================================================\n`);

    return {
      success: true,
      runDate: fullDate,
      dayMonth: searchDayMonth,
      processedCount: results.length,
      birthdays: todayBirthdays.map(f => ({
        facultyId: f.facultyId,
        name: f.name,
        deptCode: f.deptCode,
        mobile: f.mobile
      })),
      results
    };
  } catch (error) {
    console.error('[Cron Workflow Fatal Error]', error);
    return {
      success: false,
      error: error.message
    };
  }
}

/**
 * Initializes the automated daily 8:00 AM IST cron scheduler.
 * Cron expression: '0 0 8 * * *' (Minute 0, Hour 8, Every day)
 */
function initDailyCronScheduler() {
  const cronExpression = process.env.CRON_SCHEDULE || '0 0 8 * * *';
  const timezone = process.env.CRON_TIMEZONE || 'Asia/Kolkata';

  console.log(`[node-cron] ⏰ Initializing Daily 8:00 AM IST Birthday Scheduler...`);
  console.log(`[node-cron] Expression: "${cronExpression}" | Timezone: "${timezone}"`);

  cron.schedule(
    cronExpression,
    async () => {
      console.log(`\n[node-cron] 🔔 Daily 8:00 AM IST Alarm Triggered!`);
      await runDailyBirthdayWorkflow('AUTOMATED_CRON_8AM');
    },
    {
      scheduled: true,
      timezone
    }
  );

  console.log(`[node-cron] ✅ Automated 8:00 AM IST Birthday scheduler is ACTIVE 24/7.`);
}

module.exports = {
  initDailyCronScheduler,
  runDailyBirthdayWorkflow,
  getTodayDateStrings
};
