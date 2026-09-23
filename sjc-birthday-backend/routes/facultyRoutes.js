const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const Faculty = require('../models/Faculty');
const { DEPARTMENT_MAPPINGS, getDepartmentFullName } = require('../services/deptMapper');

// Permanent Multer storage setup
const uploadsDir = path.resolve(__dirname, '../uploads/faculty_photos');
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
  const allowed = /jpeg|jpg|png|webp/;
  const isExt = allowed.test(path.extname(file.originalname).toLowerCase());
  const isMime = allowed.test(file.mimetype);
  if (isExt && isMime) {
    cb(null, true);
  } else {
    cb(new Error('Only JPEG, PNG, and WebP image files are permitted.'));
  }
};

const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 }, // 5 MB max
  fileFilter
});

/**
 * GET /api/faculty/departments
 * Returns all 51 official department code mappings.
 */
router.get('/departments', (req, res) => {
  res.json({
    success: true,
    total: Object.keys(DEPARTMENT_MAPPINGS).length,
    data: DEPARTMENT_MAPPINGS
  });
});

/**
 * GET /api/faculty
 * Query 510 staff members with search, department code, and category filters.
 */
router.get('/', async (req, res) => {
  try {
    const { search, dept, category, page, limit } = req.query;
    const filter = {};

    if (dept) {
      filter.deptCode = dept.trim().toUpperCase();
    }

    if (category) {
      filter.category = category.trim().toUpperCase();
    }

    if (search) {
      const regex = new RegExp(search.trim(), 'i');
      filter.$or = [
        { name: regex },
        { facultyId: regex },
        { designation: regex },
        { mobile: regex },
        { deptCode: regex }
      ];
    }

    let query = Faculty.find(filter).sort({ deptCode: 1, name: 1 });

    if (page && limit) {
      const p = Math.max(1, parseInt(page, 10));
      const l = Math.max(1, parseInt(limit, 10));
      query = query.skip((p - 1) * l).limit(l);
    }

    const facultyList = await query.exec();
    const totalCount = await Faculty.countDocuments(filter);

    // Decorate each item with full department title
    const decorated = facultyList.map(f => ({
      ...f.toObject(),
      fullDeptName: getDepartmentFullName(f.deptCode)
    }));

    res.json({
      success: true,
      total: totalCount,
      count: decorated.length,
      data: decorated
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * GET /api/faculty/:id
 * Retrieve a single faculty member by facultyId or MongoDB _id.
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

    res.json({
      success: true,
      data: {
        ...faculty.toObject(),
        fullDeptName: getDepartmentFullName(faculty.deptCode)
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * POST /api/faculty
 * Add a new staff member to MongoDB.
 */
router.post('/', async (req, res) => {
  try {
    const { facultyId, name, deptCode, dob, mobile, category, designation, photoUrl } = req.body;

    if (!facultyId || !name || !deptCode || !dob || !mobile) {
      return res.status(400).json({
        success: false,
        error: 'Required fields missing: facultyId, name, deptCode, dob, mobile'
      });
    }

    const cleanId = String(facultyId).trim();
    const existing = await Faculty.findOne({ facultyId: cleanId });
    if (existing) {
      return res.status(409).json({
        success: false,
        error: `Staff with ID '${cleanId}' already exists in database.`
      });
    }

    const newFaculty = await Faculty.create({
      facultyId: cleanId,
      name: String(name).trim(),
      deptCode: String(deptCode).trim().toUpperCase(),
      dob: String(dob).trim(),
      mobile: String(mobile).trim(),
      category: category ? String(category).trim().toUpperCase() : 'TEACHING',
      designation: designation ? String(designation).trim() : 'Assistant Professor',
      photoUrl: photoUrl || '/default-avatar.png'
    });

    res.status(201).json({
      success: true,
      message: 'Faculty created successfully',
      data: newFaculty
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * PUT /api/faculty/:id
 * Update staff details or change photoUrl.
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

    res.json({
      success: true,
      message: 'Faculty updated successfully',
      data: updated
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * DELETE /api/faculty/:id
 * Remove staff member.
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

    res.json({
      success: true,
      message: `Staff member ${deleted.name} removed successfully.`
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * POST /api/faculty/upload
 * General photo file upload route.
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
 * One-Click Admin Photo Upload for a specific faculty member.
 * Uploads image file and immediately saves the permanent URL in MongoDB.
 */
router.post('/:id/photo', upload.single('photo'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ success: false, error: 'No image file provided' });
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

    console.log(`[Photo Hub] Updated permanent photo for ${faculty.name} (${faculty.facultyId}) -> ${publicPath}`);

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

module.exports = router;
