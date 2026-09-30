const path = require('path');
const fs = require('fs');
let getDepartmentFullName;
try {
  getDepartmentFullName = require('./deptMapper').getDepartmentFullName;
} catch (e) {
  try {
    getDepartmentFullName = require('../utils/deptMapper').getDepartmentFullName;
  } catch (e2) {
    getDepartmentFullName = (c) => `Department of ${c}`;
  }
}

let axiosInstance = null;
try {
  axiosInstance = require('axios');
} catch (e) {
  // Axios not yet installed in local container; native fetch will be used
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
 * Formats WhatsApp message according to the exact institutional specification.
 */
function formatWhatsAppMessage(staff, fullDeptName) {
  let resolvedDept = fullDeptName || getDepartmentFullName(staff.deptCode);
  let cleanDeptName = resolvedDept.replace(/^Department of\s+/i, '').trim();

  // If cleanDeptName is still a short code (e.g. 'CO'), resolve it to full name (e.g. 'Commerce')
  const mapped = getDepartmentFullName(cleanDeptName) || getDepartmentFullName(staff.deptCode);
  if (mapped && !mapped.toLowerCase().includes(`department of ${cleanDeptName.toLowerCase()}`)) {
    cleanDeptName = mapped.replace(/^Department of\s+/i, '').trim();
  } else if (cleanDeptName.length <= 4) {
    const fromCode = getDepartmentFullName(staff.deptCode);
    if (fromCode) {
      cleanDeptName = fromCode.replace(/^Department of\s+/i, '').trim();
    }
  }

  const deptString = `Department of ${cleanDeptName}`;
  const rawDesig = staff.designation || '';
  const cleanDesig = cleanDesignation(rawDesig, cleanDeptName);
  const designationLine = cleanDesig
    ? `${cleanDesig}, ${deptString}`
    : deptString;

  return `🎓 ST. JOSEPH'S COLLEGE 🎓
🎂 JOS GREETINGS 🎂
💐 WARM  BIRTHDAY WISHES 💐
Dear ${staff.name},
${designationLine}
May the Almighty shower His abundant blessings, vibrant health, enduring peace, and divine joy upon you as you continue your noble mission of forming young minds!
✨ With Prayers & Best Wishes from:
•  Rector: Rev. Dr. Pavulraj Michael SJ
•  Secretary: Rev. Dr. M. Arockiasamy Xavier SJ
•  Principal: Rev. Dr. K. Arockiam SJ
    & Standing Committee.`;
}

/**
 * Builds the official shortened birthday greeting message string.
 */
function buildGreetingMessage(staff) {
  const fullDeptName = getDepartmentFullName(staff.deptCode);
  return formatWhatsAppMessage(staff, fullDeptName);
}

/**
 * Normalizes phone number into E.164 international format for WhatsApp.
 * Automatically adds '91' country code if a 10-digit Indian mobile number is provided.
 */
function sanitizePhoneNumber(rawNumber) {
  if (!rawNumber) return '';
  let digits = String(rawNumber).replace(/[^0-9]/g, '');
  if (digits.length === 10) {
    digits = `91${digits}`;
  }
  return digits;
}

/**
 * Dispatches an automated WhatsApp message with attached PNG card via Meta Official WhatsApp Cloud API (Graph API v19.0).
 *
 * Handles:
 * - Dual-Mode Switch: Redirects to process.env.TEST_PHONE_NUMBER if TEST_MODE === 'true'.
 * - Meta Graph API endpoint: https://graph.facebook.com/v19.0/${PHONE_NUMBER_ID}/messages
 * - Public HTTPS image link or local simulation fallback if credentials are placeholder.
 *
 * @param {Object} staff - Faculty record from MongoDB
 * @param {string} cardFilePath - Local path of generated card PNG
 * @param {string} publicCardUrl - Optional public HTTPS URL for Meta media link
 */
async function sendBirthdayGreeting(staff, cardFilePath, publicCardUrl = null) {
  const messageText = buildGreetingMessage(staff);
  const isTestMode = String(process.env.TEST_MODE).toLowerCase() === 'true';

  let rawTargetPhone = staff.mobile;
  if (isTestMode && process.env.TEST_PHONE_NUMBER) {
    rawTargetPhone = process.env.TEST_PHONE_NUMBER;
    console.log(`[WhatsApp Service] ⚠️ TEST_MODE ACTIVE: Redirecting message from ${staff.name} (${staff.mobile}) to ${rawTargetPhone}`);
  }

  const recipientNumber = sanitizePhoneNumber(rawTargetPhone);
  const phoneNumberId = process.env.META_WHATSAPP_PHONE_NUMBER_ID;
  const accessToken = process.env.META_WHATSAPP_ACCESS_TOKEN;
  const apiVersion = process.env.META_GRAPH_API_VERSION || 'v19.0';

  console.log(`[WhatsApp Service] Preparing dispatch for: ${staff.name} -> Target: +${recipientNumber}`);

  // Construct public URL if not explicitly supplied
  let cardImageUrl = publicCardUrl;
  if (!cardImageUrl && cardFilePath) {
    const fileName = path.basename(cardFilePath);
    const baseUrl = process.env.SERVER_BASE_URL || 'http://localhost:5000';
    cardImageUrl = `${baseUrl.replace(/\/$/, '')}/uploads/cards/${fileName}`;
  }

  // Check if live Meta Cloud API credentials are provided
  const hasMetaCredentials =
    phoneNumberId &&
    accessToken &&
    phoneNumberId !== 'your_phone_number_id_here' &&
    accessToken !== 'your_meta_system_user_token_here';

  if (!hasMetaCredentials) {
    console.log(`[WhatsApp Service] ℹ️ Live Meta Cloud API credentials not configured. Running in SIMULATION DISPATCH mode.`);
    console.log(`------------------------ GREETING PAYLOAD ------------------------`);
    console.log(`To: +${recipientNumber}`);
    console.log(`Card Image Link: ${cardImageUrl}`);
    console.log(`Caption:\n${messageText}`);
    console.log(`------------------------------------------------------------------`);

    return {
      success: true,
      mode: 'SIMULATION_LOG',
      messageId: `wamid_sim_${Date.now()}_${staff.facultyId}`,
      recipientNumber,
      isTestMode,
      cardImageUrl,
      messageText,
      metaResponse: { status: 'simulated_success' }
    };
  }

  // Dispatch via Meta Graph API v19.0
  const metaEndpoint = `https://graph.facebook.com/${apiVersion}/${phoneNumberId}/messages`;

  // Payload with image attachment and full formatted caption
  const payload = {
    messaging_product: 'whatsapp',
    recipient_type: 'individual',
    to: recipientNumber,
    type: 'image',
    image: {
      link: cardImageUrl,
      caption: messageText
    }
  };

  try {
    let data;
    if (axiosInstance) {
      const response = await axiosInstance.post(metaEndpoint, payload, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        },
        timeout: 15000
      });
      data = response.data;
    } else {
      const response = await fetch(metaEndpoint, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });
      data = await response.json();
      if (!response.ok) {
        throw new Error(JSON.stringify(data));
      }
    }

    const messageId = data?.messages?.[0]?.id || `wamid_${Date.now()}`;
    console.log(`[WhatsApp Service] ✅ Meta Cloud API dispatch SUCCESS! Message ID: ${messageId}`);

    return {
      success: true,
      mode: 'META_CLOUD_API',
      messageId,
      recipientNumber,
      isTestMode,
      cardImageUrl,
      messageText,
      metaResponse: data
    };
  } catch (error) {
    const errorDetails = error.response ? JSON.stringify(error.response.data) : error.message;
    console.error(`[WhatsApp Service] ❌ Meta Cloud API dispatch FAILED for ${staff.name}:`, errorDetails);

    return {
      success: false,
      mode: 'META_CLOUD_API',
      error: errorDetails,
      recipientNumber,
      isTestMode,
      cardImageUrl,
      messageText
    };
  }
}

module.exports = {
  formatWhatsAppMessage,
  buildGreetingMessage,
  sanitizePhoneNumber,
  sendBirthdayGreeting
};
