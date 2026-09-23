require('dotenv').config({ path: require('path').resolve(__dirname, '../.env') });
const mongoose = require('mongoose');
const fs = require('fs');
const path = require('path');
const readline = require('readline');
const Faculty = require('../models/Faculty');

async function seedFaculty() {
  const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/sjc_birthday_db';
  console.log(`[Seed Script] Connecting to MongoDB: ${mongoUri}`);

  await mongoose.connect(mongoUri);

  const csvPath = path.resolve(__dirname, '../../app/src/main/assets/sjc_staff_directory.csv');
  if (!fs.existsSync(csvPath)) {
    console.error(`[Seed Script Error] CSV file not found at: ${csvPath}`);
    process.exit(1);
  }

  console.log(`[Seed Script] Reading CSV from: ${csvPath}`);

  const fileStream = fs.createReadStream(csvPath);
  const rl = readline.createInterface({
    input: fileStream,
    crlfDelay: Infinity
  });

  let lineCount = 0;
  let successCount = 0;
  let errorCount = 0;

  for await (const line of rl) {
    lineCount++;
    if (lineCount === 1) continue; // Skip header
    if (!line.trim()) continue;

    // Staff ID,Name,Department,Designation,DOB,Mobile,Category
    const parts = line.split(',');
    if (parts.length < 6) continue;

    const staffId = parts[0].trim();
    const name = parts[1].trim();
    const dept = parts[2].trim().toUpperCase();
    const designation = parts[3].trim();
    const dob = parts[4].trim();
    const mobile = parts[5].trim();
    const rawCategory = parts[6] ? parts[6].trim() : 'Teaching';

    let category = 'TEACHING';
    if (rawCategory.toLowerCase().includes('non')) {
      category = 'NON_TEACHING';
    } else if (rawCategory.toLowerCase().includes('jesuit')) {
      category = 'JESUIT_LEADERSHIP';
    }

    try {
      // Upsert: Preserves photoUrl if already set by Admin
      await Faculty.findOneAndUpdate(
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
        { upsert: true, new: true }
      );
      successCount++;
    } catch (err) {
      console.error(`Error saving staff ${staffId}:`, err.message);
      errorCount++;
    }
  }

  console.log(`\n======================================================`);
  console.log(`[Seed Complete] Successfully upserted ${successCount} staff members!`);
  if (errorCount > 0) console.log(`[Seed Warnings] ${errorCount} records had errors.`);
  console.log(`======================================================\n`);

  await mongoose.disconnect();
  process.exit(0);
}

seedFaculty().catch(err => {
  console.error('Fatal Seed Error:', err);
  process.exit(1);
});
