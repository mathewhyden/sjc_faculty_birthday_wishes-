const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const { getDepartmentFullName } = require('../utils/deptMapper');

const CARD_WIDTH = 1200;
const CARD_HEIGHT = 675;

/**
 * Extracts 2-character uppercase monogram initials from full faculty name.
 */
function getInitials(name) {
  if (!name) return 'SJ';
  const clean = name
    .replace(/Dr\./gi, '')
    .replace(/Rev\./gi, '')
    .replace(/Fr\./gi, '')
    .replace(/Mr\./gi, '')
    .replace(/Mrs\./gi, '')
    .replace(/Ms\./gi, '')
    .replace(/Ph\.D\./gi, '')
    .replace(/SJ/gi, '')
    .replace(/\./g, ' ')
    .trim();
  const words = clean.split(/\s+/).filter(Boolean);
  if (words.length === 0) return 'SJ';
  if (words.length === 1) return words[0].slice(0, 2).toUpperCase();
  return (words[0][0] + words[1][0]).toUpperCase();
}

/**
 * Generates the 1200x675 HD Birthday Card PNG.
 * Uses node-canvas if available, or high-performance ImageMagick CLI engine.
 */
async function generateCard(faculty, outputFilePath) {
  const expandedDept = getDepartmentFullName(faculty.deptCode);
  const designation = faculty.designation || 'Staff Member';
  const combinedDesigDept = `${designation} of ${expandedDept}`;
  const initials = getInitials(faculty.name);

  // Ensure output directory exists
  const outDir = path.dirname(outputFilePath);
  if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
  }

  // Resolve faculty photo if available
  let resolvedPhotoPath = null;
  if (faculty.photoUrl && faculty.photoUrl !== '/default-avatar.png') {
    let candidate = faculty.photoUrl;
    if (candidate.startsWith('/uploads')) {
      candidate = path.join(__dirname, '..', candidate);
    }
    if (fs.existsSync(candidate)) {
      resolvedPhotoPath = candidate;
    }
  }

  // Principal photo asset path
  const principalPhotoAsset = path.resolve(__dirname, '../../app/src/main/res/drawable/ic_principal_photo.png');
  const hasPrincipalAsset = fs.existsSync(principalPhotoAsset);

  // Try pure node-canvas first
  try {
    const { createCanvas, loadImage } = require('canvas');
    const canvas = createCanvas(CARD_WIDTH, CARD_HEIGHT);
    const ctx = canvas.getContext('2d');

    // Background Gradient
    const bgGrad = ctx.createLinearGradient(0, 0, CARD_WIDTH, CARD_HEIGHT);
    bgGrad.addColorStop(0, '#001B30');
    bgGrad.addColorStop(0.5, '#002B49');
    bgGrad.addColorStop(1, '#001628');
    ctx.fillStyle = bgGrad;
    ctx.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT);

    // Outer Gold Border (Double Line)
    ctx.strokeStyle = '#D4AF37';
    ctx.lineWidth = 4;
    ctx.strokeRect(30, 30, CARD_WIDTH - 60, CARD_HEIGHT - 60);

    ctx.strokeStyle = '#FFDF73';
    ctx.lineWidth = 1.5;
    ctx.strokeRect(40, 40, CARD_WIDTH - 80, CARD_HEIGHT - 80);

    // College Header
    ctx.textAlign = 'center';
    ctx.fillStyle = '#D4AF37';
    ctx.font = 'bold 22px Georgia, serif';
    ctx.fillText("ST. JOSEPH'S COLLEGE (AUTONOMOUS)", CARD_WIDTH / 2, 85);

    ctx.fillStyle = '#E2E8F0';
    ctx.font = '13px sans-serif';
    ctx.fillText("Special Heritage Status by UGC • Accredited A++ (Cycle IV) by NAAC • Tiruchirappalli - 620 002", CARD_WIDTH / 2, 110);

    ctx.fillStyle = '#FFDF73';
    ctx.font = 'italic 12px Georgia, serif';
    ctx.fillText("Motto: Pro Bono Et Vero (For the Good and the True)", CARD_WIDTH / 2, 130);

    // Decorative separator line under header
    ctx.strokeStyle = 'rgba(212, 175, 55, 0.4)';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(80, 148);
    ctx.lineTo(CARD_WIDTH - 80, 148);
    ctx.stroke();

    // =========================================================================
    // FACULTY PHOTO / AVATAR ON THE RIGHT SIDE (X = 980, Y = 180, D = 170)
    // =========================================================================
    const avatarCenterX = 1065;
    const avatarCenterY = 265;
    const avatarRadius = 85;

    // Gold Outer Ring
    ctx.beginPath();
    ctx.arc(avatarCenterX, avatarCenterY, avatarRadius + 3, 0, Math.PI * 2);
    ctx.strokeStyle = '#D4AF37';
    ctx.lineWidth = 6;
    ctx.stroke();

    let photoDrawn = false;
    if (resolvedPhotoPath) {
      try {
        const photoImg = await loadImage(resolvedPhotoPath);
        ctx.save();
        ctx.beginPath();
        ctx.arc(avatarCenterX, avatarCenterY, avatarRadius, 0, Math.PI * 2);
        ctx.closePath();
        ctx.clip();
        ctx.drawImage(photoImg, avatarCenterX - avatarRadius, avatarCenterY - avatarRadius, avatarRadius * 2, avatarRadius * 2);
        ctx.restore();
        photoDrawn = true;
      } catch (err) {
        console.warn('[Canvas] Photo load failed, using monogram fallback:', err.message);
      }
    }

    if (!photoDrawn) {
      // Crimson / Burgundy circle with Initials badge
      const avatarGrad = ctx.createLinearGradient(
        avatarCenterX - avatarRadius, avatarCenterY - avatarRadius,
        avatarCenterX + avatarRadius, avatarCenterY + avatarRadius
      );
      avatarGrad.addColorStop(0, '#800000');
      avatarGrad.addColorStop(1, '#4A0000');
      ctx.fillStyle = avatarGrad;
      ctx.beginPath();
      ctx.arc(avatarCenterX, avatarCenterY, avatarRadius, 0, Math.PI * 2);
      ctx.fill();

      ctx.fillStyle = '#D4AF37';
      ctx.font = 'bold 54px sans-serif';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(initials, avatarCenterX, avatarCenterY);
      ctx.textBaseline = 'alphabetic'; // reset
    }

    // =========================================================================
    // FACULTY DETAILS ON THE LEFT (X = 80px)
    // =========================================================================
    const infoStartX = 80;

    // Crimson Pill Badge
    ctx.fillStyle = '#800000';
    ctx.beginPath();
    ctx.roundRect(infoStartX, 175, 270, 32, 16);
    ctx.fill();
    ctx.strokeStyle = '#D4AF37';
    ctx.lineWidth = 1;
    ctx.stroke();

    ctx.fillStyle = '#FFDF73';
    ctx.font = 'bold 12.5px sans-serif';
    ctx.textAlign = 'left';
    ctx.fillText("★  HAPPY BIRTHDAY PROFESSOR  ★", infoStartX + 18, 196);

    // Faculty Name
    ctx.fillStyle = '#FFFFFF';
    ctx.font = 'bold 32px Georgia, serif';
    ctx.fillText(faculty.name, infoStartX, 250);

    // Single combined line: Designation of Department
    ctx.fillStyle = '#D4AF37';
    ctx.font = '19px sans-serif';
    ctx.fillText(combinedDesigDept, infoStartX, 288);

    // Scripture / Prayer Box
    ctx.fillStyle = 'rgba(15, 23, 42, 0.7)';
    ctx.beginPath();
    ctx.roundRect(infoStartX, 330, CARD_WIDTH - 160, 105, 12);
    ctx.fill();
    ctx.strokeStyle = 'rgba(212, 175, 55, 0.35)';
    ctx.lineWidth = 1;
    ctx.stroke();

    ctx.fillStyle = '#FFDF73';
    ctx.font = 'bold 14px Georgia, serif';
    ctx.fillText("✦ INSTITUTIONAL BENEDICTION & PRAYER ✦", infoStartX + 25, 360);

    ctx.fillStyle = '#E2E8F0';
    ctx.font = 'italic 16px Georgia, serif';
    ctx.fillText('"May the Lord bless you and keep you; make His face shine upon you and be gracious to you."', infoStartX + 25, 392);
    ctx.fillStyle = '#94A3B8';
    ctx.font = '13px sans-serif';
    ctx.fillText("- Numbers 6:24-25 • St. Joseph's College (Autonomous) Faculty Birthday Commemoration", infoStartX + 25, 418);

    // Divider before footer
    ctx.strokeStyle = 'rgba(212, 175, 55, 0.35)';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(65, 465);
    ctx.lineTo(CARD_WIDTH - 65, 465);
    ctx.stroke();

    // =========================================================================
    // BOTTOM-LEFT: PRINCIPAL'S PHOTO & STANDING COMMITTEE
    // =========================================================================
    const principalX = 80;
    const principalY = 490;
    const principalSize = 95;

    ctx.beginPath();
    ctx.arc(principalX + principalSize / 2, principalY + principalSize / 2, principalSize / 2 + 2, 0, Math.PI * 2);
    ctx.strokeStyle = '#D4AF37';
    ctx.lineWidth = 3;
    ctx.stroke();

    if (hasPrincipalAsset) {
      try {
        const principalImg = await loadImage(principalPhotoAsset);
        ctx.save();
        ctx.beginPath();
        ctx.arc(principalX + principalSize / 2, principalY + principalSize / 2, principalSize / 2, 0, Math.PI * 2);
        ctx.clip();
        ctx.drawImage(principalImg, principalX, principalY, principalSize, principalSize);
        ctx.restore();
      } catch (_) {}
    }

    // Label under photo
    ctx.fillStyle = '#D4AF37';
    ctx.font = 'bold 12.5px sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText("Principal & Standing Committee", principalX + principalSize / 2 + 15, 615);

    // =========================================================================
    // BOTTOM-RIGHT: LEADERSHIP GREETINGS (RECTOR -> PRINCIPAL -> SECRETARY)
    // =========================================================================
    const leadX = CARD_WIDTH - 500;
    const leadY = 480;

    ctx.fillStyle = '#152336';
    ctx.beginPath();
    ctx.roundRect(leadX, leadY, 435, 145, 10);
    ctx.fill();
    ctx.strokeStyle = 'rgba(212, 175, 55, 0.4)';
    ctx.lineWidth = 1.2;
    ctx.stroke();

    ctx.textAlign = 'left';
    ctx.fillStyle = '#FFDF73';
    ctx.font = 'bold 13.5px sans-serif';
    ctx.fillText("WITH PRAYERS & BEST WISHES FROM:", leadX + 18, leadY + 26);

    ctx.fillStyle = '#FFFFFF';
    ctx.font = 'bold 13px Georgia, serif';
    ctx.fillText("RECTOR, PRINCIPAL & SECRETARY", leadX + 18, leadY + 47);

    ctx.fillStyle = '#D4AF37';
    ctx.font = 'bold 13px sans-serif';
    ctx.fillText("• Rector:", leadX + 18, leadY + 70);
    ctx.fillStyle = '#E2E8F0';
    ctx.font = '13px sans-serif';
    ctx.fillText("Rev. Dr. Pavulraj Michael SJ", leadX + 85, leadY + 70);

    ctx.fillStyle = '#D4AF37';
    ctx.font = 'bold 13px sans-serif';
    ctx.fillText("• Principal:", leadX + 18, leadY + 91);
    ctx.fillStyle = '#E2E8F0';
    ctx.font = '13px sans-serif';
    ctx.fillText("Rev. Dr. K. Arockiam SJ", leadX + 98, leadY + 91);

    ctx.fillStyle = '#D4AF37';
    ctx.font = 'bold 13px sans-serif';
    ctx.fillText("• Secretary:", leadX + 18, leadY + 112);
    ctx.fillStyle = '#E2E8F0';
    ctx.font = '13px sans-serif';
    ctx.fillText("Rev. Dr. M. Arockiasamy Xavier SJ", leadX + 104, leadY + 112);

    ctx.fillStyle = '#94A3B8';
    ctx.font = 'italic 11px sans-serif';
    ctx.fillText("& the entire St. Joseph's College Fraternity", leadX + 18, leadY + 133);

    // Save PNG buffer to disk
    const buffer = canvas.toBuffer('image/png');
    fs.writeFileSync(outputFilePath, buffer);
    console.log(`[Canvas] Card successfully rendered: ${outputFilePath}`);
    return outputFilePath;

  } catch (canvasErr) {
    // High-performance ImageMagick CLI Fallback Engine
    console.log('[Canvas] Node-canvas not available or error, falling back to ImageMagick engine:', canvasErr.message);

    const safeName = faculty.name.replace(/["\\]/g, '');
    const safeDesig = combinedDesigDept.replace(/["\\]/g, '');

    const cmd = `convert -size 1200x675 xc:"#002038" \\
      -fill "#001B30" -draw "rectangle 0,0 1200,675" \\
      -fill "#002B49" -draw "roundrectangle 30,30 1170,645 15,15" \\
      -stroke "#D4AF37" -strokewidth 4 -fill none -draw "roundrectangle 30,30 1170,645 15,15" \\
      -stroke "#FFDF73" -strokewidth 1.5 -fill none -draw "roundrectangle 40,40 1160,635 12,12" \\
      -stroke none -fill "#D4AF37" -font "DejaVu-Serif-Bold" -pointsize 24 -gravity north -annotate +0+55 "ST. JOSEPH'S COLLEGE (AUTONOMOUS)" \\
      -fill "#E2E8F0" -font "DejaVu-Sans" -pointsize 13 -gravity north -annotate +0+90 "Special Heritage Status by UGC • Accredited A++ (Cycle IV) by NAAC • Tiruchirappalli - 620 002" \\
      -fill "#FFDF73" -font "DejaVu-Serif" -pointsize 12 -gravity north -annotate +0+112 "Motto: Pro Bono Et Vero (For the Good and the True)" \\
      -fill "#800000" -stroke "#D4AF37" -strokewidth 1 -draw "roundrectangle 80,175 350,207 16,16" \\
      -stroke none -fill "#FFDF73" -font "DejaVu-Sans-Bold" -pointsize 12.5 -gravity northwest -annotate +100+182 "★  HAPPY BIRTHDAY PROFESSOR  ★" \\
      -fill "#FFFFFF" -font "DejaVu-Serif-Bold" -pointsize 30 -gravity northwest -annotate +80+225 "${safeName}" \\
      -fill "#D4AF37" -font "DejaVu-Sans-Bold" -pointsize 18 -gravity northwest -annotate +80+268 "${safeDesig}" \\
      -fill "#800000" -stroke "#D4AF37" -strokewidth 5 -draw "circle 1065,265 1065,350" \\
      -stroke none -fill "#D4AF37" -font "DejaVu-Sans-Bold" -pointsize 54 -gravity northwest -annotate +1025+235 "${initials}" \\
      -fill "#0F172A" -stroke "#D4AF37" -strokewidth 1 -draw "roundrectangle 80,330 1120,435 10,10" \\
      -stroke none -fill "#FFDF73" -font "DejaVu-Serif-Bold" -pointsize 14 -gravity northwest -annotate +105+345 "✦ INSTITUTIONAL BENEDICTION & PRAYER ✦" \\
      -fill "#E2E8F0" -font "DejaVu-Serif" -pointsize 16 -gravity northwest -annotate +105+375 "\\"May the Lord bless you and keep you; make His face shine upon you and be gracious to you.\\"" \\
      -fill "#94A3B8" -font "DejaVu-Sans" -pointsize 13 -gravity northwest -annotate +105+402 "- Numbers 6:24-25 • St. Joseph's College (Autonomous)" \\
      -fill "#152336" -stroke "#D4AF37" -strokewidth 1 -draw "roundrectangle 700,480 1135,625 10,10" \\
      -stroke none -fill "#FFDF73" -font "DejaVu-Sans-Bold" -pointsize 13 -gravity northwest -annotate +718+496 "WITH PRAYERS & BEST WISHES FROM:" \\
      -fill "#FFFFFF" -font "DejaVu-Serif-Bold" -pointsize 13 -gravity northwest -annotate +718+516 "RECTOR, PRINCIPAL & SECRETARY" \\
      -fill "#D4AF37" -font "DejaVu-Sans-Bold" -pointsize 12.5 -gravity northwest -annotate +718+538 "• Rector: Rev. Dr. Pavulraj Michael SJ" \\
      -fill "#D4AF37" -font "DejaVu-Sans-Bold" -pointsize 12.5 -gravity northwest -annotate +718+558 "• Principal: Rev. Dr. K. Arockiam SJ" \\
      -fill "#D4AF37" -font "DejaVu-Sans-Bold" -pointsize 12.5 -gravity northwest -annotate +718+578 "• Secretary: Rev. Dr. M. Arockiasamy Xavier SJ" \\
      -fill "#94A3B8" -font "DejaVu-Sans" -pointsize 11 -gravity northwest -annotate +718+598 "& the entire St. Joseph's College Fraternity" \\
      -fill "#D4AF37" -stroke "#D4AF37" -strokewidth 3 -draw "circle 130,540 130,585" \\
      -stroke none -fill "#D4AF37" -font "DejaVu-Sans-Bold" -pointsize 12 -gravity northwest -annotate +40+598 "Principal & Standing Committee" \\
      "${outputFilePath}"`;

    execSync(cmd, { stdio: 'ignore' });
    console.log(`[ImageMagick] Card generated successfully: ${outputFilePath}`);
    return outputFilePath;
  }
}

module.exports = {
  generateCard,
  getInitials
};
