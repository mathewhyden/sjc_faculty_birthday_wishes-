const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
let getDepartmentFullName;
try {
  getDepartmentFullName = require('./deptMapper').getDepartmentFullName;
} catch (e) {
  try {
    getDepartmentFullName = require('../utils/deptMapper').getDepartmentFullName;
  } catch (e2) {
    getDepartmentFullName = (code) => `Department of ${code}`;
  }
}

// Try loading node-canvas if compiled on host, otherwise fallback seamlessly to ImageMagick
let createCanvas, loadImage;
try {
  const canvasPkg = require('canvas');
  createCanvas = canvasPkg.createCanvas;
  loadImage = canvasPkg.loadImage;
} catch (e) {
  // Graceful fallback to ImageMagick
}

function cleanDesignation(rawDesig, cleanDept) {
  if (!rawDesig) return '';
  let cleaned = String(rawDesig).trim();
  const dept = String(cleanDept || '').replace(/^Department of\s+/i, '').trim();
  if (dept) {
    const escaped = dept.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    cleaned = cleaned.replace(new RegExp(`\\s+(of|in|for)\\s+${escaped}.*$`, 'i'), '').trim();
    cleaned = cleaned.replace(new RegExp(`[,\\-\\s]+${escaped}$`, 'i'), '').trim();
  }
  cleaned = cleaned.replace(/\s+(of|in)\s+[A-Za-z\s.&]+$/i, (match) => {
    const suffix = match.trim();
    const suffixDept = suffix.replace(/^(of|in)\s+/i, '').trim();
    if (dept.toLowerCase().includes(suffixDept.toLowerCase()) || suffixDept.toLowerCase().includes(dept.toLowerCase())) {
      return '';
    }
    return match;
  }).trim();
  return cleaned;
}

/**
 * 1200 x 675 HD Card Generator using Node-Canvas.
 * Adheres strictly to the executive alignment & layout specifications.
 */
async function generateCardWithNodeCanvas(faculty, outputPath) {
  const width = 1200;
  const height = 675;
  const canvas = createCanvas(width, height);
  const ctx = canvas.getContext('2d');

  const fullDeptName = getDepartmentFullName(faculty.deptCode);
  const cleanDeptName = fullDeptName.replace(/^Department of\s+/i, '').trim();
  const deptString = `Department of ${cleanDeptName}`;
  const rawDesignation = faculty.designation || '';
  const cleanDesig = cleanDesignation(rawDesignation, cleanDeptName);
  const combinedTitle = cleanDesig ? `${cleanDesig}, ${deptString}` : deptString;

  // 1. Deep Royal Navy Luxury Gradient Background
  const bgGrad = ctx.createLinearGradient(0, 0, width, height);
  bgGrad.addColorStop(0, '#002B49'); // Royal Navy
  bgGrad.addColorStop(0.55, '#001B30'); // Deep Navy
  bgGrad.addColorStop(1, '#0A1118'); // Midnight Slate
  ctx.fillStyle = bgGrad;
  ctx.fillRect(0, 0, width, height);

  // 2. Double Ornate Metallic Gold Framing
  ctx.save();
  ctx.strokeStyle = '#D4AF37';
  ctx.lineWidth = 4;
  ctx.strokeRect(28, 28, width - 56, height - 56);

  ctx.strokeStyle = 'rgba(212, 175, 55, 0.4)';
  ctx.lineWidth = 1.5;
  ctx.strokeRect(38, 38, width - 76, height - 76);

  // Four corner decorative accents
  ctx.fillStyle = '#D4AF37';
  [
    [48, 48],
    [width - 48, 48],
    [48, height - 48],
    [width - 48, height - 48],
  ].forEach(([cx, cy]) => {
    ctx.beginPath();
    ctx.arc(cx, cy, 4, 0, Math.PI * 2);
    ctx.fill();
  });
  ctx.restore();

  // 3. TOP INSTITUTIONAL HEADER
  const crestX = 110;
  const crestY = 95;

  ctx.save();
  ctx.beginPath();
  ctx.arc(crestX, crestY, 24, 0, Math.PI * 2);
  ctx.fillStyle = '#800000';
  ctx.fill();
  ctx.lineWidth = 2.5;
  ctx.strokeStyle = '#D4AF37';
  ctx.stroke();

  ctx.font = 'bold 15px Georgia, serif';
  ctx.fillStyle = '#FFDF73';
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText('IHS', crestX, crestY + 1);
  ctx.restore();

  // College Title: Line 1 (SINGLE LINE): "ST. JOSEPH'S COLLEGE TIRUCHIRAPPALLI"
  const headerCenterX = width / 2;
  ctx.font = 'bold 28px Georgia, serif';
  ctx.fillStyle = '#FFFFFF';
  ctx.textAlign = 'center';
  ctx.fillText("ST. JOSEPH'S COLLEGE TIRUCHIRAPPALLI", headerCenterX, 72);

  // Line 2 (Motto): "Motto: \"Pro Bono Et Vero\" (For the Good and the True)"
  ctx.font = 'italic 16px Georgia, serif';
  ctx.fillStyle = '#CBD5E1';
  ctx.fillText('Motto: "Pro Bono Et Vero" (For the Good and the True)', headerCenterX, 104);

  // Line 3 (Header Badge): "★ JOS BIRTHDAY WISHES ★"
  ctx.font = 'bold 24px Arial, sans-serif';
  ctx.fillStyle = '#D4AF37';
  ctx.fillText("★ JOS BIRTHDAY WISHES ★", headerCenterX, 140);

  // Gold Divider Ribbon
  ctx.strokeStyle = 'rgba(212, 175, 55, 0.5)';
  ctx.lineWidth = 1.5;
  ctx.beginPath();
  ctx.moveTo(100, 162);
  ctx.lineTo(1100, 162);
  ctx.stroke();

  // =========================================================================
  // 2. FACULTY AVATAR & DETAILS
  // - Avatar Circle: Positioned fixed on the far RIGHT side (X = 980px, Y = 180px, Diameter = 150px with Gold border)
  //   Top-Left (980, 180), Center (1055, 255), Radius = 75px
  // =========================================================================
  const avatarLeft = 980;
  const avatarTop = 180;
  const avatarDiameter = 150;
  const avatarRadius = 75;
  const avatarCenterX = avatarLeft + avatarRadius; // 1055
  const avatarCenterY = avatarTop + avatarRadius; // 255

  let photoRendered = false;
  if (faculty.photoUrl && faculty.photoUrl !== '/default-avatar.png') {
    try {
      let resolvedPhotoPath = faculty.photoUrl;
      if (!resolvedPhotoPath.startsWith('http://') && !resolvedPhotoPath.startsWith('https://')) {
        resolvedPhotoPath = path.resolve(__dirname, '..', faculty.photoUrl.replace(/^\//, ''));
      }
      if (fs.existsSync(resolvedPhotoPath) || resolvedPhotoPath.startsWith('http')) {
        const photoImg = await loadImage(resolvedPhotoPath);
        ctx.save();
        ctx.beginPath();
        ctx.arc(avatarCenterX, avatarCenterY, avatarRadius, 0, Math.PI * 2);
        ctx.closePath();
        ctx.clip();
        ctx.drawImage(
          photoImg,
          avatarCenterX - avatarRadius,
          avatarCenterY - avatarRadius,
          avatarRadius * 2,
          avatarRadius * 2
        );
        ctx.restore();
        photoRendered = true;
      }
    } catch (photoErr) {
      console.warn(`[Canvas] Could not load photo for ${faculty.name}, falling back to initial badge.`);
    }
  }

  // Fallback: Initial Badge with Burgundy gradient and Gold Monogram
  if (!photoRendered) {
    ctx.save();
    const avatarGrad = ctx.createLinearGradient(
      avatarCenterX - avatarRadius,
      avatarCenterY - avatarRadius,
      avatarCenterX + avatarRadius,
      avatarCenterY + avatarRadius
    );
    avatarGrad.addColorStop(0, '#800000');
    avatarGrad.addColorStop(1, '#4A0000');

    ctx.beginPath();
    ctx.arc(avatarCenterX, avatarCenterY, avatarRadius, 0, Math.PI * 2);
    ctx.fillStyle = avatarGrad;
    ctx.fill();

    const initials = extractInitials(faculty.name);
    ctx.font = 'bold 50px Arial, sans-serif';
    ctx.fillStyle = '#D4AF37';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(initials, avatarCenterX, avatarCenterY + 2);
    ctx.restore();
  }

  // 4px Gold Border (#D4AF37)
  ctx.save();
  ctx.beginPath();
  ctx.arc(avatarCenterX, avatarCenterY, avatarRadius, 0, Math.PI * 2);
  ctx.strokeStyle = '#D4AF37';
  ctx.lineWidth = 4;
  ctx.stroke();
  ctx.restore();

  // =========================================================================
  // 2. PROFESSOR DETAILS (LEFT SIDE ANCHOR)
  // - Left Margin X = 80px
  // - Badge ("★ HAPPY BIRTHDAY PROFESSOR ★"): Y = 180px, Height = 32px, Pill-shaped
  // - Full Name: Y = 235px (baseline 245px), Left-aligned at X = 80px
  // - Designation & Department: Y = 280px (baseline), Left-aligned at X = 80px
  //   Format: "${faculty.designation}, ${fullDeptName}"
  // - Max text width = 800px so text never touches or overlaps the photo avatar
  // =========================================================================
  const leftMarginX = 80;
  const maxTextWidth = 800;

  // Pill Badge: Y = 180px, Height = 34px, Width = 300px
  const pillX = leftMarginX;
  const pillY = 180;
  const pillW = 300;
  const pillH = 34;
  const r = pillH / 2;

  ctx.save();
  ctx.beginPath();
  ctx.moveTo(pillX + r, pillY);
  ctx.lineTo(pillX + pillW - r, pillY);
  ctx.quadraticCurveTo(pillX + pillW, pillY, pillX + pillW, pillY + r);
  ctx.lineTo(pillX + pillW, pillY + pillH - r);
  ctx.quadraticCurveTo(pillX + pillW, pillY + pillH, pillX + pillW - r, pillY + pillH);
  ctx.lineTo(pillX + r, pillY + pillH);
  ctx.quadraticCurveTo(pillX, pillY + pillH, pillX, pillY + pillH - r);
  ctx.lineTo(pillX, pillY + r);
  ctx.quadraticCurveTo(pillX, pillY, pillX + r, pillY);
  ctx.closePath();
  ctx.fillStyle = '#800000';
  ctx.fill();
  ctx.strokeStyle = '#D4AF37';
  ctx.lineWidth = 1.5;
  ctx.stroke();

  ctx.font = 'bold 12px Arial, sans-serif';
  ctx.fillStyle = '#FFDF73';
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText('★ HAPPY BIRTHDAY PROFESSOR ★', pillX + pillW / 2, pillY + pillH / 2);
  ctx.restore();

  // Full Name: Left-aligned at X = 80px, baseline at 252px (breathing room from badge)
  ctx.save();
  ctx.textAlign = 'left';
  ctx.textBaseline = 'alphabetic';
  ctx.font = 'bold 32px Georgia, serif';
  ctx.fillStyle = '#FFFFFF';
  ctx.fillText(faculty.name, leftMarginX, 252);

  // Designation & Department (SINGLE LINE): Left-aligned at X = 80px, baseline at 285px
  ctx.font = 'bold 20px Arial, sans-serif';
  ctx.fillStyle = '#D4AF37';
  ctx.fillText(combinedTitle, leftMarginX, 285);
  ctx.restore();

  // =========================================================================
  // 3. MIDDLE GREETING QUOTE BOX
  // - Container Box: X = 60px, Y = 345px, Width = 1080px, Height = 120px, Corner Radius = 16px
  // - Line 1: Y = 385px, Centered
  // - Line 2: Y = 425px, Centered
  // =========================================================================
  const quoteBoxX = 60;
  const quoteBoxY = 345;
  const quoteBoxW = 1080;
  const quoteBoxH = 120;
  const quoteR = 16;

  ctx.save();
  ctx.beginPath();
  ctx.roundRect ? ctx.roundRect(quoteBoxX, quoteBoxY, quoteBoxW, quoteBoxH, quoteR) : ctx.rect(quoteBoxX, quoteBoxY, quoteBoxW, quoteBoxH);
  ctx.fillStyle = '#152C47';
  ctx.fill();
  ctx.strokeStyle = 'rgba(212, 175, 55, 0.6)';
  ctx.lineWidth = 2;
  ctx.stroke();

  // Line 1: Y = 385px, Centered
  ctx.textAlign = 'center';
  ctx.textBaseline = 'alphabetic';
  ctx.font = 'bold 26px Georgia, serif';
  ctx.fillStyle = '#FFDF73';
  ctx.fillText('💐 WISHING YOU A VERY HAPPY & BLESSED BIRTHDAY! 💐', 600, 385);

  // Line 2: Y = 418px & 442px, Centered
  ctx.font = 'italic 19px Georgia, serif';
  ctx.fillStyle = '#FFFFFF';
  ctx.fillText('“May the Almighty bless you with vibrant health, lasting peace, and divine joy', 600, 418);
  ctx.fillText('as you continue your noble mission of shaping minds at St. Joseph\'s!”', 600, 442);
  ctx.restore();

  // Divider Line above footer
  ctx.strokeStyle = 'rgba(212, 175, 55, 0.3)';
  ctx.lineWidth = 1;
  ctx.beginPath();
  ctx.moveTo(60, 475);
  ctx.lineTo(1140, 475);
  ctx.stroke();

  // =========================================================================
  // 4. BOTTOM-LEFT PRINCIPAL SECTION (SIDE-BY-SIDE FLEX LAYOUT)
  // - Principal Photo Circle: Center X = 110px, Center Y = 555px, Radius = 40px (Diameter 80px)
  // - Label Text (Positioned to the RIGHT of photo at X = 170px):
  //   * Line 1 (Y = 545px): "Principal & Standing Committee" (Bold Gold)
  //   * Line 2 (Y = 570px): "St. Joseph's College (Autonomous)" (Muted Slate)
  // =========================================================================
  const pCenterX = 110;
  const pCenterY = 555;
  const pRadius = 40;

  let principalDrawn = false;
  const principalCircleRing = path.resolve(__dirname, '../uploads/principal_circle_80.png');
  if (fs.existsSync(principalCircleRing)) {
    try {
      const pRingImg = await loadImage(principalCircleRing);
      ctx.drawImage(pRingImg, pCenterX - pRadius, pCenterY - pRadius, pRadius * 2, pRadius * 2);
      principalDrawn = true;
    } catch (err) {}
  }

  if (!principalDrawn) {
    ctx.save();
    const principalGrad = ctx.createLinearGradient(
      pCenterX - pRadius,
      pCenterY - pRadius,
      pCenterX + pRadius,
      pCenterY + pRadius
    );
    principalGrad.addColorStop(0, '#0A192F');
    principalGrad.addColorStop(1, '#001A33');
    ctx.beginPath();
    ctx.arc(pCenterX, pCenterY, pRadius, 0, Math.PI * 2);
    ctx.fillStyle = principalGrad;
    ctx.fill();

    ctx.font = 'bold 28px Georgia, serif';
    ctx.fillStyle = '#D4AF37';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('SJ', pCenterX, pCenterY);

    ctx.beginPath();
    ctx.arc(pCenterX, pCenterY, pRadius, 0, Math.PI * 2);
    ctx.strokeStyle = '#D4AF37';
    ctx.lineWidth = 3;
    ctx.stroke();
    ctx.restore();
  }

  // Side-by-side Text at X = 170px
  ctx.save();
  ctx.textAlign = 'left';
  ctx.textBaseline = 'alphabetic';
  ctx.font = 'bold 17.5px Arial, sans-serif';
  ctx.fillStyle = '#D4AF37';
  ctx.fillText('Rev. Dr. K. Arockiam SJ', 170, 545);

  ctx.font = '14px Georgia, serif';
  ctx.fillStyle = '#FFFFFF';
  ctx.fillText('Principal, Academic Head, SJC', 170, 570);
  ctx.restore();

  // =========================================================================
  // 5. BOTTOM-RIGHT LEADERSHIP CONTAINER
  // - Container Box: X = 600px, Y = 490px, Width = 540px, Height = 135px, Corner Radius = 12px
  // - Header (Y = 515px, X = 620px): "WITH PRAYERS & BEST WISHES FROM:"
  // - Sub-Header (Y = 535px, X = 620px): "RECTOR, SECRETARY & PRINCIPAL"
  // - Ordered List Items (X = 620px):
  //   * Y = 560px: "• Rector: Rev. Dr. Pavulraj Michael SJ"
  //   * Y = 580px: "• Secretary: Rev. Dr. M. Arockiasamy Xavier SJ"
  //   * Y = 600px: "• Principal: Rev. Dr. K. Arockiam SJ"
  //   * Y = 618px: "& Standing Committee"
  // =========================================================================
  const lBoxX = 600;
  const lBoxY = 480;
  const lBoxW = 540;
  const lBoxH = 152;
  const lBoxR = 12;

  ctx.save();
  ctx.beginPath();
  ctx.roundRect ? ctx.roundRect(lBoxX, lBoxY, lBoxW, lBoxH, lBoxR) : ctx.rect(lBoxX, lBoxY, lBoxW, lBoxH);
  ctx.fillStyle = '#152336';
  ctx.fill();
  ctx.strokeStyle = 'rgba(212, 175, 55, 0.4)';
  ctx.lineWidth = 1.2;
  ctx.stroke();

  const lTextX = 624;

  // Header (Y = 504px, X = 624px)
  ctx.textAlign = 'left';
  ctx.textBaseline = 'alphabetic';
  ctx.font = 'bold 13px Arial, sans-serif';
  ctx.fillStyle = '#FFDF73';
  ctx.fillText('WITH PRAYERS & BEST WISHES FROM:', lTextX, 504);

  // Sub-Header (Y = 524px, X = 624px)
  ctx.font = 'bold 12.5px Georgia, serif';
  ctx.fillStyle = '#FFFFFF';
  ctx.fillText('RECTOR, SECRETARY & PRINCIPAL', lTextX, 524);

  const rector = process.env.RECTOR_NAME || 'Rev. Dr. Pavulraj Michael SJ';
  const secretary = process.env.SECRETARY_NAME || 'Rev. Dr. M. Arockiasamy Xavier SJ';
  const principal = process.env.PRINCIPAL_NAME || 'Rev. Dr. K. Arockiam SJ';

  ctx.font = '12px Arial, sans-serif';
  ctx.fillStyle = '#E2E8F0';
  ctx.fillText(`•  Rector: ${rector}`, lTextX, 548);
  ctx.fillText(`•  Secretary: ${secretary}`, lTextX, 568);
  ctx.fillText(`•  Principal: ${principal}`, lTextX, 588);

  // Standing Committee: indented to align with names after bullets, comfortable 24px bottom clearance
  ctx.font = 'italic 12px Georgia, serif';
  ctx.fillStyle = '#FFDF73';
  ctx.fillText('& Standing Committee', lTextX + 14, 610);
  ctx.restore();

  // Save to file
  const outDir = path.dirname(outputPath);
  if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
  }

  const buffer = canvas.toBuffer('image/png');
  fs.writeFileSync(outputPath, buffer);
  return outputPath;
}

/**
 * Fallback Card Generator using ImageMagick CLI if canvas binary compilation fails.
 * Guarantees zero downtime across cloud providers.
 */
function generateCardWithImageMagick(faculty, outputPath) {
  return new Promise((resolve, reject) => {
    const fullDeptName = getDepartmentFullName(faculty.deptCode);
    const cleanDeptName = fullDeptName.replace(/^Department of\s+/i, '').trim();
    const rawDesignation = faculty.designation || '';
    const cleanDesig = cleanDesignation(rawDesignation, cleanDeptName);
    const combinedTitle = cleanDesig ? `${cleanDesig}, ${deptString}` : deptString;
    const initials = extractInitials(faculty.name);

    const rector = process.env.RECTOR_NAME || 'Rev. Dr. Pavulraj Michael SJ';
    const secretary = process.env.SECRETARY_NAME || 'Rev. Dr. M. Arockiasamy Xavier SJ';
    const principal = process.env.PRINCIPAL_NAME || 'Rev. Dr. K. Arockiam SJ';

    const safeName = faculty.name.replace(/["\\]/g, '');
    const safeTitle = combinedTitle.replace(/["\\]/g, '');

    const outDir = path.dirname(outputPath);
    if (!fs.existsSync(outDir)) {
      fs.mkdirSync(outDir, { recursive: true });
    }

    const convert = spawn('convert', [
      '-size', '1200x675',
      'gradient:#002B49-#001B30',
      // Outer & Inner Gold Borders
      '-stroke', '#D4AF37', '-strokewidth', '4', '-fill', 'none',
      '-draw', 'roundrectangle 28,28 1172,647 16,16',
      '-stroke', '#66D4AF37', '-strokewidth', '1.5', '-fill', 'none',
      '-draw', 'roundrectangle 38,38 1162,637 12,12',

      // SJC Crest IHS Emblem
      '-stroke', '#D4AF37', '-strokewidth', '2.5', '-fill', '#800000',
      '-draw', 'circle 110,95 110,71',
      '-stroke', 'none', '-fill', '#FFDF73', '-font', 'DejaVu-Sans-Bold', '-pointsize', '15',
      '-gravity', 'NorthWest', '-annotate', '+96+87', 'IHS',

      // College Title: Line 1, Line 2 (Motto), Line 3 (Header Badge) - Centered at X=600 (+0)
      '-fill', '#FFFFFF', '-font', 'DejaVu-Serif-Bold', '-pointsize', '28',
      '-gravity', 'North', '-annotate', '+0+48', "ST. JOSEPH'S COLLEGE TIRUCHIRAPPALLI",
      '-fill', '#CBD5E1', '-font', 'DejaVu-Serif', '-pointsize', '15',
      '-annotate', '+0+84', 'Motto: "Pro Bono Et Vero" (For the Good and the True)',
      '-fill', '#D4AF37', '-font', 'DejaVu-Sans-Bold', '-pointsize', '24',
      '-annotate', '+0+118', '★ JOS BIRTHDAY WISHES ★',
      '-stroke', '#50D4AF37', '-strokewidth', '1.5',
      '-draw', 'line 100,162 1100,162',

      // 1. Right Avatar: Fixed X = 980, Y = 180, Diameter = 150 (Center 1055, 255)
      '-stroke', '#D4AF37', '-strokewidth', '4', '-fill', '#800000',
      '-draw', 'circle 1055,255 1055,180',
      '-stroke', 'none', '-fill', '#D4AF37', '-font', 'DejaVu-Sans-Bold', '-pointsize', '50',
      '-gravity', 'NorthWest', '-annotate', '+1021+225', initials,

      // 2. Professor Details: Left Anchor at X = 80px
      // Pill Badge: Y = 180px, Height = 34px, Width = 300px (Text centered vertically & horizontally)
      '-stroke', '#D4AF37', '-strokewidth', '1.5', '-fill', '#800000',
      '-draw', 'roundrectangle 80,180 380,214 17,17',
      '-stroke', 'none', '-fill', '#FFDF73', '-font', 'DejaVu-Sans-Bold', '-pointsize', '12',
      '-gravity', 'NorthWest', '-annotate', '+114+190', '★ HAPPY BIRTHDAY PROFESSOR ★',

      // Name & Title: Evenly spaced with comfortable breathing room
      '-fill', '#FFFFFF', '-font', 'DejaVu-Serif-Bold', '-pointsize', '32',
      '-annotate', '+80+232', safeName,
      '-fill', '#D4AF37', '-font', 'DejaVu-Sans-Bold', '-pointsize', '20',
      '-annotate', '+80+278', safeTitle,

      // 3. Greeting Quote Box: X = 60, Y = 345, W = 1080, H = 120
      '-stroke', '#99D4AF37', '-strokewidth', '2', '-fill', '#152C47',
      '-draw', 'roundrectangle 60,345 1140,465 16,16',
      '-stroke', 'none',
      '-gravity', 'North',
      '-fill', '#FFDF73', '-font', 'DejaVu-Serif-Bold', '-pointsize', '26',
      '-annotate', '+0+360', '💐 WISHING YOU A VERY HAPPY & BLESSED BIRTHDAY! 💐',
      '-fill', '#FFFFFF', '-font', 'DejaVu-Serif', '-pointsize', '18',
      '-annotate', '+0+396', '“May the Almighty bless you with vibrant health, lasting peace, and divine joy',
      '-annotate', '+0+422', 'as you continue your noble mission of shaping minds at St. Joseph\'s!”',

      // Divider Line above footer
      '-stroke', '#33D4AF37', '-strokewidth', '1',
      '-draw', 'line 60,472 1140,472',

      // 4. Bottom-Left Principal Side-by-side: Center X = 110, Center Y = 556, Radius = 40
      '-stroke', '#D4AF37', '-strokewidth', '3', '-fill', '#0A192F',
      '-draw', 'circle 110,556 110,516',
      '-stroke', 'none', '-fill', '#D4AF37', '-font', 'DejaVu-Serif-Bold', '-pointsize', '28',
      '-gravity', 'NorthWest', '-annotate', '+92+543', 'SJ',
      '-fill', '#D4AF37', '-font', 'DejaVu-Sans-Bold', '-pointsize', '17.5',
      '-annotate', '+170+544', 'Rev. Dr. K. Arockiam SJ',
      '-fill', '#FFFFFF', '-font', 'DejaVu-Serif', '-pointsize', '14',
      '-annotate', '+170+568', 'Principal, Academic Head, SJC',

      // 5. Bottom-Right Leadership Container: X = 600, Y = 480, W = 540, H = 152 (Room for Standing Committee)
      '-stroke', '#66D4AF37', '-strokewidth', '1.2', '-fill', '#152336',
      '-draw', 'roundrectangle 600,480 1140,632 12,12',
      '-stroke', 'none',
      '-fill', '#FFDF73', '-font', 'DejaVu-Sans-Bold', '-pointsize', '13',
      '-annotate', '+624+494', 'WITH PRAYERS & BEST WISHES FROM:',
      '-fill', '#FFFFFF', '-font', 'DejaVu-Serif-Bold', '-pointsize', '12.5',
      '-annotate', '+624+514', 'RECTOR, SECRETARY & PRINCIPAL',
      '-fill', '#E2E8F0', '-font', 'DejaVu-Sans', '-pointsize', '12',
      '-annotate', '+624+536', `•  Rector: ${rector}`,
      '-annotate', '+624+555', `•  Secretary: ${secretary}`,
      '-annotate', '+624+574', `•  Principal: ${principal}`,
      '-fill', '#FFDF73', '-font', 'DejaVu-Serif', '-pointsize', '12',
      '-annotate', '+638+596', '& Standing Committee',
      outputPath
    ]);

    convert.on('close', (code) => {
      if (code === 0) {
        const principalRing = path.resolve(__dirname, '../uploads/principal_circle_80.png');
        if (fs.existsSync(principalRing)) {
          const comp = spawn('composite', ['-geometry', '+70+515', principalRing, outputPath, outputPath]);
          comp.on('close', () => resolve(outputPath));
          comp.on('error', () => resolve(outputPath));
        } else {
          resolve(outputPath);
        }
      } else {
        reject(new Error(`ImageMagick convert exited with code ${code}`));
      }
    });

    convert.on('error', (err) => {
      reject(err);
    });
  });
}

function extractInitials(name) {
  if (!name) return 'SJ';
  const clean = name
    .replace(/(Dr\.|Rev\.|Fr\.|Mr\.|Mrs\.|Ms\.|Ph\.D\.|SJ|\.)/gi, ' ')
    .trim();
  const parts = clean.split(/\s+/).filter(Boolean);
  if (parts.length === 0) return 'SJ';
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[1][0]).toUpperCase();
}

async function generateCard(faculty, outputPath) {
  if (createCanvas) {
    return await generateCardWithNodeCanvas(faculty, outputPath);
  }
  console.log('[Canvas Engine] Node-canvas fallback to ImageMagick');
  return await generateCardWithImageMagick(faculty, outputPath);
}

module.exports = {
  generateCard,
  generateCardWithNodeCanvas,
  generateCardWithImageMagick,
  extractInitials,
};
