const fs = require('fs');
const path = require('path');
const { execFile } = require('child_process');
const { getDepartmentFullName } = require('./deptMapper');

/**
 * Extracts initials from staff name for gold/crimson fallback badge.
 * e.g. "Dr. G. GENIFER SILVENA" -> "GG"
 */
function extractInitials(name) {
  if (!name) return 'SJ';
  const clean = name.replace(/Dr\.|Rev\.|Fr\.|Mr\.|Mrs\.|Ms\.|SJ|\./gi, ' ').trim();
  const parts = clean.split(/\s+/).filter(Boolean);
  if (parts.length === 0) return 'SJ';
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[1][0]).toUpperCase();
}

/**
 * Primary Canvas Engine using 'canvas' (Cairo-based native bindings)
 */
async function generateCardWithNodeCanvas(faculty, outputPath) {
  const { createCanvas, loadImage } = require('canvas');

  const width = 1200;
  const height = 675;
  const canvas = createCanvas(width, height);
  const ctx = canvas.getContext('2d');

  const fullDeptName = getDepartmentFullName(faculty.deptCode);
  const cleanDeptName = fullDeptName.replace(/^Department of\s+/i, '').trim();
  const designation = faculty.designation || 'Staff Member';
  const combinedTitle = `${designation} Department of ${cleanDeptName}`;

  // 1. Deep Royal Navy Luxury Gradient Background
  const bgGrad = ctx.createLinearGradient(0, 0, width, height);
  bgGrad.addColorStop(0, '#001426');
  bgGrad.addColorStop(0.5, '#002B49');
  bgGrad.addColorStop(1, '#001020');
  ctx.fillStyle = bgGrad;
  ctx.fillRect(0, 0, width, height);

  // Decorative subtle radiant glow
  const radialGlow = ctx.createRadialGradient(980, 265, 20, 980, 265, 300);
  radialGlow.addColorStop(0, 'rgba(212, 175, 55, 0.18)');
  radialGlow.addColorStop(1, 'rgba(0, 43, 73, 0)');
  ctx.fillStyle = radialGlow;
  ctx.fillRect(0, 0, width, height);

  // 2. Ornate Double Gold Outer Border
  ctx.strokeStyle = '#D4AF37';
  ctx.lineWidth = 5;
  ctx.strokeRect(18, 18, width - 36, height - 36);

  ctx.strokeStyle = 'rgba(212, 175, 55, 0.45)';
  ctx.lineWidth = 1.5;
  ctx.strokeRect(26, 26, width - 52, height - 52);

  // Corner Gold Accents
  const cornerSize = 25;
  ctx.fillStyle = '#D4AF37';
  // Top-left
  ctx.fillRect(26, 26, cornerSize, 3);
  ctx.fillRect(26, 26, 3, cornerSize);
  // Top-right
  ctx.fillRect(width - 26 - cornerSize, 26, cornerSize, 3);
  ctx.fillRect(width - 29, 26, 3, cornerSize);
  // Bottom-left
  ctx.fillRect(26, height - 29, cornerSize, 3);
  ctx.fillRect(26, height - 26 - cornerSize, 3, cornerSize);
  // Bottom-right
  ctx.fillRect(width - 26 - cornerSize, height - 29, cornerSize, 3);
  ctx.fillRect(width - 29, height - 26 - cornerSize, 3, cornerSize);

  // 3. TOP HEADER (Centered)
  // Crest Monogram Badge (X = 600, Y = 62, Radius = 28)
  const crestX = 600;
  const crestY = 56;
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

  // College Title
  ctx.font = 'bold 27px Georgia, serif';
  ctx.fillStyle = '#D4AF37';
  ctx.textAlign = 'center';
  ctx.fillText("ST. JOSEPH'S COLLEGE (AUTONOMOUS)", 600, 102);

  // Subtitle & Accreditations
  ctx.font = '13px Arial, sans-serif';
  ctx.fillStyle = '#E2E8F0';
  ctx.fillText("TIRUCHIRAPPALLI - 620 002 • TAMIL NADU, INDIA", 600, 123);

  // Latin College Motto & Ribbon
  ctx.font = 'italic bold 12.5px Georgia, serif';
  ctx.fillStyle = '#FFDF73';
  ctx.fillText("~ Pro Bono Et Vero ~  (For the Good and the True • Estd. 1844)", 600, 142);

  // Gold Divider Ribbon
  ctx.strokeStyle = 'rgba(212, 175, 55, 0.6)';
  ctx.lineWidth = 1.2;
  ctx.beginPath();
  ctx.moveTo(140, 154);
  ctx.lineTo(1060, 154);
  ctx.stroke();

  // 4. CENTER BODY - BIRTHDAY CELEBRANT DETAILS
  // A) Birthday Greeting Pill
  ctx.save();
  const pillX = 80;
  const pillY = 186;
  const pillW = 280;
  const pillH = 32;
  const r = 16;
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
  ctx.lineWidth = 1.8;
  ctx.stroke();

  ctx.font = 'bold 12.5px Arial, sans-serif';
  ctx.fillStyle = '#FFDF73';
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText('★  WARMEST BIRTHDAY GREETINGS  ★', pillX + pillW / 2, pillY + pillH / 2);
  ctx.restore();

  // B) Faculty Name (Left-Aligned at X = 80px)
  ctx.textAlign = 'left';
  ctx.textBaseline = 'alphabetic';
  ctx.font = 'bold 36px Georgia, serif';
  ctx.fillStyle = '#FFFFFF';
  ctx.shadowColor = 'rgba(0, 0, 0, 0.7)';
  ctx.shadowBlur = 8;
  ctx.fillText(faculty.name, 80, 266);
  ctx.shadowBlur = 0; // reset

  // C) Single Combined Line: "${faculty.designation} of ${fullDeptName}"
  // NOTE: Staff ID is REMOVED completely as required.
  ctx.font = '600 20px Arial, sans-serif';
  ctx.fillStyle = '#FFDF73';
  ctx.fillText(combinedTitle, 80, 305);

  // Inspirational blessing note
  ctx.font = 'italic 16px Georgia, serif';
  ctx.fillStyle = '#CBD5E1';
  ctx.fillText(
    '"May the Almighty shower His abundant blessings, vibrant health, enduring peace,',
    80,
    352
  );
  ctx.fillText(
    'and divine joy upon you as you continue your noble mission of forming young minds!"',
    80,
    378
  );

  // Thin separator line
  ctx.strokeStyle = 'rgba(212, 175, 55, 0.35)';
  ctx.lineWidth = 1;
  ctx.beginPath();
  ctx.moveTo(80, 415);
  ctx.lineTo(1120, 415);
  ctx.stroke();

  // 5. CENTER BODY - RIGHT SIDE AVATAR (X = 980px, Y = 180px, 170x170 px)
  const avatarCenterX = 980 + 85; // 1065px
  const avatarCenterY = 180 + 85; // 265px
  const avatarRadius = 85; // Diameter = 170px

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

  // Fallback: Initial Badge ("GG") with Burgundy gradient and Gold Monogram
  if (!photoRendered) {
    ctx.save();
    const avatarGrad = ctx.createLinearGradient(
      avatarCenterX - avatarRadius,
      avatarCenterY - avatarRadius,
      avatarCenterX + avatarRadius,
      avatarCenterY + avatarRadius
    );
    avatarGrad.addColorStop(0, '#990000');
    avatarGrad.addColorStop(1, '#4A0000');

    ctx.beginPath();
    ctx.arc(avatarCenterX, avatarCenterY, avatarRadius, 0, Math.PI * 2);
    ctx.fillStyle = avatarGrad;
    ctx.fill();

    const initials = extractInitials(faculty.name);
    ctx.font = 'bold 54px Georgia, serif';
    ctx.fillStyle = '#D4AF37';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(initials, avatarCenterX, avatarCenterY + 3);
    ctx.restore();
  }

  // 170px Avatar Ornate Gold Border & Outer Ring
  ctx.save();
  ctx.beginPath();
  ctx.arc(avatarCenterX, avatarCenterY, avatarRadius, 0, Math.PI * 2);
  ctx.strokeStyle = '#D4AF37';
  ctx.lineWidth = 4;
  ctx.stroke();

  ctx.beginPath();
  ctx.arc(avatarCenterX, avatarCenterY, avatarRadius + 6, 0, Math.PI * 2);
  ctx.strokeStyle = 'rgba(212, 175, 55, 0.45)';
  ctx.lineWidth = 1.5;
  ctx.stroke();
  ctx.restore();

  // 6. BOTTOM-LEFT CORNER (X = 80px, Y = 500px)
  // Principal Photo Bitmap (100x100) + Bold Gold Text Label
  const pSize = 100;
  const pRadius = pSize / 2; // 50px
  const pCenterX = 80 + pRadius; // 130px
  const pCenterY = 460 + pRadius; // 510px

  // Draw Principal photo / monogram circle
  ctx.save();
  const principalGrad = ctx.createLinearGradient(
    pCenterX - pRadius,
    pCenterY - pRadius,
    pCenterX + pRadius,
    pCenterY + pRadius
  );
  principalGrad.addColorStop(0, '#003366');
  principalGrad.addColorStop(1, '#001A33');
  ctx.beginPath();
  ctx.arc(pCenterX, pCenterY, pRadius, 0, Math.PI * 2);
  ctx.fillStyle = principalGrad;
  ctx.fill();

  ctx.font = 'bold 30px Georgia, serif';
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

  // Label: "Principal & Standing Committee"
  ctx.textAlign = 'left';
  ctx.textBaseline = 'alphabetic';
  ctx.font = 'bold 15px Georgia, serif';
  ctx.fillStyle = '#D4AF37';
  ctx.fillText('Principal & Standing Committee', 80 + pSize + 16, 502);

  ctx.font = '13px Arial, sans-serif';
  ctx.fillStyle = '#CBD5E1';
  ctx.fillText('St. Joseph\'s College (Autonomous)', 80 + pSize + 16, 524);
  ctx.fillText('Tiruchirappalli, Tamil Nadu', 80 + pSize + 16, 542);

  // 7. BOTTOM-RIGHT CORNER (Leadership in exact specified order)
  const rightX = 720;
  ctx.textAlign = 'left';
  ctx.font = 'bold 14px Georgia, serif';
  ctx.fillStyle = '#D4AF37';
  ctx.fillText('✨ With Prayers & Best Wishes from:', rightX, 452);

  ctx.font = '13.5px Arial, sans-serif';
  ctx.fillStyle = '#FFFFFF';

  const rector = process.env.RECTOR_NAME || 'Rev. Dr. Pavulraj Michael SJ';
  const principal = process.env.PRINCIPAL_NAME || 'Rev. Dr. K. Arockiam SJ';
  const secretary = process.env.SECRETARY_NAME || 'Rev. Dr. M. Arockiasamy Xavier SJ';

  // 1. Rector
  ctx.fillText(`• Rector: ${rector}`, rightX, 480);
  // 2. Secretary
  ctx.fillText(`• Secretary: ${secretary}`, rightX, 506);
  // 3. Principal
  ctx.fillText(`• Principal: ${principal}`, rightX, 532);

  ctx.font = 'italic 12px Georgia, serif';
  ctx.fillStyle = '#FFDF73';
  ctx.fillText('& the entire St. Joseph\'s College (Autonomous) Fraternity.', rightX, 560);

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
    const designation = faculty.designation || 'Staff Member';
    const combinedTitle = `${designation} Department of ${cleanDeptName}`;
    const initials = extractInitials(faculty.name);

    const rector = process.env.RECTOR_NAME || 'Rev. Dr. Pavulraj Michael SJ';
    const secretary = process.env.SECRETARY_NAME || 'Rev. Dr. M. Arockiasamy Xavier SJ';
    const principal = process.env.PRINCIPAL_NAME || 'Rev. Dr. K. Arockiam SJ';

    const safeName = faculty.name.replace(/["\\]/g, '');
    const safeTitle = combinedTitle.replace(/["\\]/g, '');

    const outDir = path.dirname(outputPath);
    if (!fs.existsSync(outDir)) fs.mkdirSync(outDir, { recursive: true });

    const args = [
      '-size', '1200x675',
      'xc:#001B30',
      '-fill', '#002B49', '-draw', 'rectangle 20,20 1180,655',
      '-stroke', '#D4AF37', '-strokewidth', '4', '-fill', 'none', '-draw', 'rectangle 18,18 1182,657',
      '-stroke', '#D4AF37', '-strokewidth', '1', '-fill', 'none', '-draw', 'rectangle 25,25 1175,650',
      // Header
      '-stroke', 'none', '-fill', '#800000', '-draw', 'circle 600,56 600,80',
      '-stroke', '#D4AF37', '-strokewidth', '2', '-fill', 'none', '-draw', 'circle 600,56 600,80',
      '-stroke', 'none', '-fill', '#FFDF73', '-font', 'DejaVu-Sans-Bold', '-pointsize', '15',
      '-gravity', 'North', '-annotate', '+0+47', 'IHS',
      '-fill', '#D4AF37', '-pointsize', '26', '-annotate', '+0+88', "ST. JOSEPH'S COLLEGE (AUTONOMOUS)",
      '-fill', '#E2E8F0', '-font', 'DejaVu-Sans', '-pointsize', '13', '-annotate', '+0+118', 'TIRUCHIRAPPALLI - 620 002',
      '-fill', '#FFDF73', '-font', 'DejaVu-Sans', '-pointsize', '12', '-annotate', '+0+138', '~ Pro Bono Et Vero ~',
      // Divider
      '-stroke', '#D4AF37', '-strokewidth', '1', '-draw', 'line 140,156 1060,156',
      // Pill
      '-stroke', '#D4AF37', '-strokewidth', '2', '-fill', '#800000', '-draw', 'roundrectangle 80,186 360,218 16,16',
      '-stroke', 'none', '-fill', '#FFDF73', '-font', 'DejaVu-Sans-Bold', '-pointsize', '12',
      '-gravity', 'NorthWest', '-annotate', '+95+196', '★ WARMEST BIRTHDAY GREETINGS ★',
      // Name & Title
      '-fill', '#FFFFFF', '-font', 'DejaVu-Sans-Bold', '-pointsize', '34', '-annotate', '+80+240', safeName,
      '-fill', '#FFDF73', '-font', 'DejaVu-Sans-Bold', '-pointsize', '20', '-annotate', '+80+288', safeTitle,
      // Blessing
      '-fill', '#CBD5E1', '-font', 'DejaVu-Sans', '-pointsize', '15',
      '-annotate', '+80+340', 'May the Almighty shower His abundant blessings, vibrant health, enduring peace,',
      '-annotate', '+80+364', 'and divine joy upon you as you continue your noble mission of forming young minds!',
      // Avatar on Right
      '-stroke', 'none', '-fill', '#800000', '-draw', 'circle 1065,265 1065,350',
      '-stroke', '#D4AF37', '-strokewidth', '4', '-fill', 'none', '-draw', 'circle 1065,265 1065,350',
      '-stroke', 'none', '-fill', '#D4AF37', '-font', 'DejaVu-Sans-Bold', '-pointsize', '52',
      '-gravity', 'NorthWest', '-annotate', '+1028+244', initials,
      // Bottom-Left Principal Label
      '-stroke', 'none', '-fill', '#001A33', '-draw', 'circle 130,510 130,560',
      '-stroke', '#D4AF37', '-strokewidth', '3', '-fill', 'none', '-draw', 'circle 130,510 130,560',
      '-stroke', 'none', '-fill', '#D4AF37', '-font', 'DejaVu-Sans-Bold', '-pointsize', '28',
      '-gravity', 'NorthWest', '-annotate', '+112+498', 'SJ',
      '-fill', '#D4AF37', '-font', 'DejaVu-Sans-Bold', '-pointsize', '15',
      '-annotate', '+195+495', 'Principal & Standing Committee',
      '-fill', '#CBD5E1', '-font', 'DejaVu-Sans', '-pointsize', '13',
      '-annotate', '+195+520', "St. Joseph's College (Autonomous)",
      // Bottom-Right Leadership
      '-fill', '#D4AF37', '-font', 'DejaVu-Sans-Bold', '-pointsize', '14',
      '-annotate', '+720+445', '✨ With Prayers & Best Wishes from:',
      '-fill', '#FFFFFF', '-font', 'DejaVu-Sans', '-pointsize', '13.5',
      '-annotate', '+720+475', `• Rector: ${rector}`,
      '-annotate', '+720+502', `• Secretary: ${secretary}`,
      '-annotate', '+720+529', `• Principal: ${principal}`,
      '-fill', '#FFDF73', '-font', 'DejaVu-Sans', '-pointsize', '12',
      '-annotate', '+720+558', "& the entire St. Joseph's College Fraternity.",
      outputPath
    ];

    execFile('convert', args, (error) => {
      if (error) {
        console.error('[ImageMagick Error]', error);
        return reject(error);
      }
      resolve(outputPath);
    });
  });
}

/**
 * Universal Card Generator facade:
 * Tries high-fidelity Cairo canvas first; if unavailable, falls back to ImageMagick smoothly.
 */
async function generateCard(faculty, outputPath) {
  try {
    return await generateCardWithNodeCanvas(faculty, outputPath);
  } catch (err) {
    console.warn('[Canvas Engine] Node-canvas fallback to ImageMagick:', err.message);
    return await generateCardWithImageMagick(faculty, outputPath);
  }
}

module.exports = {
  generateCard,
  extractInitials
};
