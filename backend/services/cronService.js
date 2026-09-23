const cron = require('node-cron');
const path = require('path');
const Faculty = require('../models/Faculty');
const BirthdayLog = require('../models/BirthdayLog');
const { generateCard } = require('./canvasService');
const { sendBirthdayGreeting } = require('./whatsappService');
const { getDepartmentFullName } = require('../utils/deptMapper');

/**
 * Returns today's formatted DD-MM and DD-MM-YYYY in Asia/Kolkata timezone.
 */
function getTodayDateStrings() {
  const now = new Date();
  // Format based on current date
  const day = String(now.getDate()).padStart(2, '0');
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const year = now.getFullYear();

  return {
    dayMonth: `${day}-${month}`,
    fullDate: `${day}-${month}-${year}`
  };
}

/**
 * Core workflow to process birthdays for a specific day.
 */
async function runDailyBirthdayWorkflow(triggerType = 'AUTOMATED_CRON', targetDayMonth = null) {
  const { dayMonth, fullDate } = getTodayDateStrings();
  const searchDayMonth = targetDayMonth || dayMonth;

  console.log(`\n======================================================`);
  console.log(`[Cron Workflow] Initiating Birthday Run for: ${searchDayMonth} (${triggerType})`);
  console.log(`======================================================`);

  try {
    // 1. Fetch all faculty members from MongoDB
    const allFaculty = await Faculty.find({});
    console.log(`[Cron Workflow] Total faculty in database: ${allFaculty.length}`);

    // Filter staff whose DOB matches DD-MM
    const todayBirthdays = allFaculty.filter(staff => {
      if (!staff.dob) return false;
      const parts = staff.dob.split('-');
      if (parts.length < 2) return false;
      const staffDM = `${parts[0].padStart(2, '0')}-${parts[1].padStart(2, '0')}`;
      return staffDM === searchDayMonth;
    });

    console.log(`[Cron Workflow] Birthdays detected for today: ${todayBirthdays.length}`);

    const results = [];

    // 2. Process each birthday professor
    for (const faculty of todayBirthdays) {
      const expandedDept = getDepartmentFullName(faculty.deptCode);
      console.log(`\n-> Processing: ${faculty.name} | Dept: ${faculty.deptCode} (${expandedDept}) | DOB: ${faculty.dob}`);

      // Destination card path
      const cardsDir = path.join(__dirname, '../uploads/generated_cards');
      const cardFileName = `card_${faculty.facultyId}_${fullDate.replace(/-/g, '_')}.png`;
      const cardFilePath = path.join(cardsDir, cardFileName);

      let cardPath = '';
      let whatsappStatus = 'PENDING';
      let messageId = '';
      let errorMessage = '';

      try {
        // Render 1200x675 HD Card PNG via Canvas engine using staff's stored photoUrl
        await generateCard(faculty, cardFilePath);
        cardPath = `/uploads/generated_cards/${cardFileName}`;

        // Dispatch WhatsApp message with attached PNG card
        const dispatchRes = await sendBirthdayGreeting(faculty, cardFilePath);
        if (dispatchRes.success) {
          whatsappStatus = 'SUCCESS';
          messageId = dispatchRes.messageId || '';
        } else {
          whatsappStatus = 'FAILED';
          errorMessage = dispatchRes.error || 'WhatsApp delivery error';
        }
      } catch (err) {
        console.error(`[Cron Workflow Error] Failed processing ${faculty.name}:`, err.message);
        whatsappStatus = 'FAILED';
        errorMessage = err.message;
      }

      // 3. Log to MongoDB BirthdayLog collection
      try {
        const logEntry = await BirthdayLog.create({
          runDate: fullDate,
          triggerType,
          facultyId: faculty.facultyId,
          facultyName: faculty.name,
          deptCode: faculty.deptCode,
          mobile: faculty.mobile,
          cardImagePath: cardPath,
          whatsappStatus,
          whatsappMessageId: messageId,
          errorMessage
        });
        results.push(logEntry);
      } catch (logErr) {
        console.error('[Cron Workflow] Failed to save BirthdayLog:', logErr.message);
      }
    }

    console.log(`\n[Cron Workflow] Completed! Processed ${results.length} faculty greetings.`);
    return {
      success: true,
      processedCount: results.length,
      birthdays: todayBirthdays.map(f => f.name),
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
 * Initializes the node-cron scheduler.
 * Default: Every day at 00:01 AM ('0 1 0 * * *').
 */
function initCronScheduler() {
  const cronExpression = process.env.CRON_SCHEDULE || '0 1 0 * * *';
  const timezone = process.env.CRON_TIMEZONE || 'Asia/Kolkata';

  console.log(`[node-cron] Initializing midnight birthday scheduler...`);
  console.log(`[node-cron] Schedule: "${cronExpression}" (Timezone: ${timezone})`);

  cron.schedule(cronExpression, async () => {
    console.log(`\n[node-cron] Trigger fired at 00:01 AM!`);
    await runDailyBirthdayWorkflow('AUTOMATED_CRON');
  }, {
    scheduled: true,
    timezone
  });

  console.log(`[node-cron] Birthday scheduler is ACTIVE and monitoring 24/7.`);
}

module.exports = {
  initCronScheduler,
  runDailyBirthdayWorkflow,
  getTodayDateStrings
};
