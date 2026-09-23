require('dotenv').config({ path: require('path').resolve(__dirname, '../.env') });
const mongoose = require('mongoose');
const fs = require('fs');
const path = require('path');
const readline = require('readline');
const Faculty = require('../models/Faculty');

async function seedDatabase() {
  const mongoUri =
    process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017/sjc_birthday_db';

  console.log(`\n========================================================================`);
  console.log(`🎓 ST. JOSEPH'S COLLEGE (AUTONOMOUS) - FACULTY SEED SCRIPT`);
  console.log(`Connecting to MongoDB URI: ${mongoUri.replace(/\/\/.*@/, '//<credentials>@')}`);
  console.log(`========================================================================\n`);

  try {
    await mongoose.connect(mongoUri);
    console.log(`✅ MongoDB connection established.`);
  } catch (connErr) {
    console.error(`❌ MongoDB connection failed:`, connErr.message);
    process.exit(1);
  }

  // Find CSV file
  const candidatePaths = [
    path.resolve(__dirname, 'sjc_staff_directory.csv'),
    path.resolve(__dirname, '../../app/src/main/assets/sjc_staff_directory.csv'),
    path.resolve(__dirname, '../data/sjc_staff_directory.csv')
  ];

  let csvPath = candidatePaths.find(p => fs.existsSync(p));
  if (!csvPath) {
    console.error(`❌ Staff directory CSV file not found in candidate paths.`);
    process.exit(1);
  }

  console.log(`📄 Reading staff directory from: ${csvPath}`);

  const fileStream = fs.createReadStream(csvPath);
  const rl = readline.createInterface({
    input: fileStream,
    crlfDelay: Infinity
  });

  let lineCount = 0;
  let insertedCount = 0;
  let updatedCount = 0;
  let errorCount = 0;

  for await (const rawLine of rl) {
    lineCount++;
    const line = rawLine.trim();
    if (lineCount === 1) continue; // Skip CSV Header row
    if (!line) continue;

    // Schema: Staff ID, Name, Department, Designation, DOB, Mobile, Category
    const parts = line.split(',');
    if (parts.length < 6) {
      console.warn(`Line ${lineCount} malformed: "${line}"`);
      errorCount++;
      continue;
    }

    const staffId = parts[0].trim();
    const name = parts[1].trim();
    const dept = parts[2].trim().toUpperCase();
    const designation = parts[3].trim();
    const dob = parts[4].trim();
    const mobile = parts[5].trim();
    const rawCategory = parts[6] ? parts[6].trim().toLowerCase() : 'teaching';

    let category = 'TEACHING';
    if (rawCategory.includes('non')) {
      category = 'NON_TEACHING';
    } else if (rawCategory.includes('jesuit')) {
      category = 'JESUIT_LEADERSHIP';
    }

    try {
      // Upsert: Updates core directory fields while PRESERVING custom uploaded photoUrl
      const res = await Faculty.updateOne(
        { facultyId: staffId },
        {
          $set: {
            name,
            deptCode: dept,
            designation,
            dob,
            mobile,
            category
          },
          $setOnInsert: {
            facultyId: staffId,
            photoUrl: '/default-avatar.png'
          }
        },
        { upsert: true }
      );

      if (res.upsertedCount > 0) {
        insertedCount++;
      } else {
        updatedCount++;
      }
    } catch (err) {
      console.error(`Error saving staff ${staffId} (${name}):`, err.message);
      errorCount++;
    }
  }

  const totalInDb = await Faculty.countDocuments();

  console.log(`\n========================================================================`);
  console.log(`🎉 SEEDING COMPLETED SUCCESSFULLY!`);
  console.log(`   - New Staff Inserted: ${insertedCount}`);
  console.log(`   - Existing Staff Refreshed: ${updatedCount}`);
  console.log(`   - Total Faculty in MongoDB: ${totalInDb}`);
  if (errorCount > 0) console.log(`   - Errors Encountered: ${errorCount}`);
  console.log(`========================================================================\n`);

  await mongoose.disconnect();
  process.exit(0);
}

seedDatabase().catch(err => {
  console.error('Fatal seed failure:', err);
  process.exit(1);
});
