const mongoose = require('mongoose');

const FacultySchema = new mongoose.Schema({
  facultyId: {
    type: String,
    required: true,
    unique: true,
    trim: true,
    index: true
  },
  name: {
    type: String,
    required: true,
    trim: true
  },
  deptCode: {
    type: String,
    required: true,
    trim: true,
    uppercase: true,
    index: true
  },
  dob: {
    type: String,
    required: true, // Format: DD-MM-YYYY
    trim: true,
    index: true
  },
  mobile: {
    type: String,
    required: true,
    trim: true
  },
  category: {
    type: String,
    enum: ['TEACHING', 'NON_TEACHING', 'JESUIT_LEADERSHIP'],
    default: 'TEACHING'
  },
  designation: {
    type: String,
    default: 'Staff Member',
    trim: true
  },
  photoUrl: {
    type: String,
    default: '/default-avatar.png',
    trim: true
  }
}, {
  timestamps: true
});

// Helper to check if today matches DOB DD-MM
FacultySchema.methods.isBirthdayToday = function (targetDayMonth = null) {
  if (!this.dob) return false;
  const parts = this.dob.split('-');
  if (parts.length < 2) return false;
  const staffDayMonth = `${parts[0].padStart(2, '0')}-${parts[1].padStart(2, '0')}`;

  if (targetDayMonth) {
    return staffDayMonth === targetDayMonth;
  }

  const now = new Date();
  const todayDay = String(now.getDate()).padStart(2, '0');
  const todayMonth = String(now.getMonth() + 1).padStart(2, '0');
  const currentDayMonth = `${todayDay}-${todayMonth}`;

  return staffDayMonth === currentDayMonth;
};

module.exports = mongoose.model('Faculty', FacultySchema);
