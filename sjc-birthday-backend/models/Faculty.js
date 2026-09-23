const mongoose = require('mongoose');

const FacultySchema = new mongoose.Schema(
  {
    facultyId: {
      type: String,
      required: [true, 'Staff ID is required'],
      unique: true,
      trim: true,
      index: true
    },
    name: {
      type: String,
      required: [true, 'Full name is required'],
      trim: true
    },
    deptCode: {
      type: String,
      required: [true, 'Department code is required'],
      trim: true,
      uppercase: true,
      index: true
    },
    dob: {
      type: String,
      required: [true, 'Date of birth is required (format: DD-MM-YYYY)'],
      trim: true,
      index: true
    },
    mobile: {
      type: String,
      required: [true, 'Mobile number is required'],
      trim: true
    },
    category: {
      type: String,
      enum: ['TEACHING', 'NON_TEACHING', 'JESUIT_LEADERSHIP'],
      default: 'TEACHING',
      index: true
    },
    designation: {
      type: String,
      default: 'Assistant Professor',
      trim: true
    },
    photoUrl: {
      type: String,
      default: '/default-avatar.png',
      trim: true
    }
  },
  {
    timestamps: true
  }
);

// Helper method to extract DD-MM for fast birthday matching
FacultySchema.methods.getBirthdayDayMonth = function () {
  if (!this.dob) return null;
  const parts = this.dob.split('-');
  if (parts.length < 2) return null;
  return `${parts[0].padStart(2, '0')}-${parts[1].padStart(2, '0')}`;
};

module.exports = mongoose.model('Faculty', FacultySchema);
