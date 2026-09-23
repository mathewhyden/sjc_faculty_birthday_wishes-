const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const Faculty = require('../models/Faculty');

// Configure Multer permanent storage
const uploadsDir = path.join(__dirname, '../uploads/faculty_photos');
if (!fs.existsSync(uploadsDir)) {
  fs.mkdirSync(uploadsDir, { recursive: true });
}

const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, uploadsDir);
  },
  filename: function (req, file, cb) {
    const ext = path.extname(file.originalname).toLowerCase() || '.jpg';
    const facultyId = req.params.id || req.body.facultyId || 'faculty';
    const cleanId = String(facultyId).replace(/[^a-zA-Z0-9_-]/g, '');
    cb(null, `photo_${cleanId}_${Date.now()}${ext}`);
  }
});

const fileFilter = (req, file, cb) => {
  const allowedTypes = /jpeg|jpg|png|webp/;
  const extname = allowedTypes.test(path.extname(file.originalname).toLowerCase());
  const mimetype = allowedTypes.test(file.mimetype);

  if (extname && mimetype) {
    return cb(null, true);
  } else {
    cb(new Error('Only JPEG, PNG, and WebP images are allowed!'));
  }
};

const upload = multer({
  storage: storage,
  limits: { fileSize: 5 * 1024 * 1024 }, // 5MB limit
  fileFilter: fileFilter
});

/**
 * GET /api/faculty
 * Retrieve all 510 staff members with search, department, and category filters.
 */
router.get('/', async (req, res) => {
  try {
    const { search, dept, category, limit, page } = req.query;
    const query = {};

    if (dept) {
      query.deptCode = dept.trim().toUpperCase();
    }

    if (category) {
      query.category = category.trim().toUpperCase();
    }

    if (search) {
      const searchRegex = new RegExp(search.trim(), 'i');
      query.$or = [
        { name: searchRegex },
        { facultyId: searchRegex },
        { designation: searchRegex },
        { mobile: searchRegex },
        { deptCode: searchRegex }
      ];
    }

    let facultyQuery = Faculty.find(query).sort({ deptCode: 1, name: 1 });

    if (limit && page) {
      const l = parseInt(limit, 10);
      const p = parseInt(page, 10);
      facultyQuery = facultyQuery.skip((p - 1) * l).limit(l);
    }

    const facultyList = await facultyQuery.exec();
    const totalCount = await Faculty.countDocuments(query);

    res.json({
      success: true,
      total: totalCount,
      count: facultyList.length,
      data: facultyList
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * GET /api/faculty/:id
 * Retrieve a single faculty member by MongoDB ObjectId or facultyId.
 */
router.get('/:id', async (req, res) => {
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

    res.json({ success: true, data: faculty });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * POST /api/faculty
 * Add a new staff member.
 */
router.post('/', async (req, res) => {
  try {
    const { facultyId, name, deptCode, dob, mobile, category, designation, photoUrl } = req.body;

    if (!facultyId || !name || !deptCode || !dob || !mobile) {
      return res.status(400).json({
        success: false,
        error: 'facultyId, name, deptCode, dob, and mobile are required fields'
      });
    }

    // Check duplicate
    const existing = await Faculty.findOne({ facultyId: facultyId.trim() });
    if (existing) {
      return res.status(409).json({
        success: false,
        error: `Faculty member with ID ${facultyId} already exists`
      });
    }

    const newFaculty = await Faculty.create({
      facultyId: facultyId.trim(),
      name: name.trim(),
      deptCode: deptCode.trim().toUpperCase(),
      dob: dob.trim(),
      mobile: mobile.trim(),
      category: category ? category.trim().toUpperCase() : 'TEACHING',
      designation: designation ? designation.trim() : 'Staff Member',
      photoUrl: photoUrl || '/default-avatar.png'
    });

    res.status(201).json({ success: true, data: newFaculty });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * PUT /api/faculty/:id
 * Edit staff details or update photo URL.
 */
router.put('/:id', async (req, res) => {
  try {
    const id = req.params.id;
    let query = { facultyId: id };

    if (id.match(/^[0-9a-fA-F]{24}$/)) {
      query = { $or: [{ _id: id }, { facultyId: id }] };
    }

    const updateData = { ...req.body };
    if (updateData.deptCode) updateData.deptCode = updateData.deptCode.toUpperCase();
    if (updateData.category) updateData.category = updateData.category.toUpperCase();

    const updated = await Faculty.findOneAndUpdate(query, updateData, { new: true });

    if (!updated) {
      return res.status(404).json({ success: false, error: 'Faculty member not found' });
    }

    res.json({ success: true, data: updated });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * POST /api/faculty/upload
 * General photo upload route (Multer) -> saves to /uploads/faculty_photos folder.
 */
router.post('/upload', upload.single('photo'), (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ success: false, error: 'No image file uploaded' });
    }

    const publicPath = `/uploads/faculty_photos/${req.file.filename}`;
    res.json({
      success: true,
      message: 'Photo uploaded successfully',
      fileName: req.file.filename,
      photoUrl: publicPath
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * POST /api/faculty/:id/photo
 * Admin Photo Upload API for a specific faculty member.
 * Uploads image and updates faculty.photoUrl in MongoDB immediately.
 */
router.post('/:id/photo', upload.single('photo'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ success: false, error: 'No image file uploaded' });
    }

    const id = req.params.id;
    let query = { facultyId: id };
    if (id.match(/^[0-9a-fA-F]{24}$/)) {
      query = { $or: [{ _id: id }, { facultyId: id }] };
    }

    const publicPath = `/uploads/faculty_photos/${req.file.filename}`;

    const faculty = await Faculty.findOneAndUpdate(
      query,
      { photoUrl: publicPath },
      { new: true }
    );

    if (!faculty) {
      return res.status(404).json({ success: false, error: 'Faculty member not found' });
    }

    res.json({
      success: true,
      message: `Photo updated successfully for ${faculty.name}`,
      photoUrl: publicPath,
      faculty
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * DELETE /api/faculty/:id
 */
router.delete('/:id', async (req, res) => {
  try {
    const id = req.params.id;
    let query = { facultyId: id };
    if (id.match(/^[0-9a-fA-F]{24}$/)) {
      query = { $or: [{ _id: id }, { facultyId: id }] };
    }

    const deleted = await Faculty.findOneAndDelete(query);
    if (!deleted) {
      return res.status(404).json({ success: false, error: 'Faculty member not found' });
    }

    res.json({ success: true, message: `Faculty ${deleted.name} removed successfully` });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
