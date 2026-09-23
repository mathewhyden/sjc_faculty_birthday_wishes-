# St. Joseph's College (Autonomous), Tiruchirappalli
# 24/7 Automated Faculty Birthday Wishes Backend (`sjc-birthday-backend`)

A 100% standalone, production-ready Node.js Express server designed to run 24/7 on cloud hosting (**Render.com**, **Railway.app**, or **Docker Container**) with zero dependency on local machines.

---

## 📁 Standalone Directory Structure

```
sjc-birthday-backend/
├── server.js                      # Express Server Entry, CORS, Port binding 0.0.0.0
├── package.json                   # Dependencies: express, mongoose, node-cron, canvas, axios, multer, dotenv, cors
├── .env.example                   # Environment variables template
├── render.yaml                    # Render Cloud Service Blueprint config
├── Dockerfile                     # Linux Container Config for Cloud Server
├── models/
│   ├── Faculty.js                 # MongoDB Schema for Staff & Permanent photoUrl
│   └── BirthdayLog.js             # MongoDB Schema logging WhatsApp dispatch transactions
├── routes/
│   ├── facultyRoutes.js           # CRUD APIs for 510 staff members & photo upload
│   └── birthdayRoutes.js          # Today's birthdays, 7-day upcoming radar, trigger now API
├── services/
│   ├── deptMapper.js              # 51-Department Dictionary & Code Resolver
│   ├── canvasService.js           # 1200x675 HD PNG Card Generator Engine
│   ├── cronService.js             # Automated 8:00 AM IST daily birthday scheduler
│   └── whatsappService.js         # Meta Official WhatsApp Cloud API dispatcher
└── scripts/
    ├── seedFaculty.js             # CSV Data Importer seeding 510 staff members to MongoDB
    └── sjc_staff_directory.csv    # Directory of 510 staff records
```

---

## ⚡ Key Features & Specifications

1. **Automated Daily 8:00 AM IST Cron Job (`services/cronService.js`)**
   - Scheduled with `node-cron` using `0 0 8 * * *` in `Asia/Kolkata` timezone.
   - At 8:00 AM sharp every day, it automatically:
     1. Queries MongoDB Atlas for staff whose DOB matches `DD-MM`.
     2. Resolves department code using the 51-department dictionary.
     3. Renders a dynamic 1200x675 HD PNG gift card using the faculty's stored photo.
     4. Dispatches an official greeting message with attached card via Meta WhatsApp Cloud API.
     5. Records audit logs in MongoDB `BirthdayLog` collection.

2. **1200x675 HD Canvas Gift Card Engine (`services/canvasService.js`)**
   - **Header**: Centered SJC Crest, "ST. JOSEPH'S COLLEGE (AUTONOMOUS)", "TIRUCHIRAPPALLI - 620 002", and Latin motto "~ Pro Bono Et Vero ~".
   - **Celebrant Section**:
     - **Right Side (X = 980px, Y = 180px)**: Circular cropped avatar Bitmap ($170 \times 170\text{ px}$) with ornate gold border. If photo is missing, dynamically generates a monogram badge (e.g. `"GG"`).
     - **Left Side (X = 80px)**: Faculty Name in bold serif, followed by the single line: `"${faculty.designation} of ${fullDeptName}"` *(Staff ID is removed)*.
   - **Bottom-Left (X = 80px, Y = 500px)**: Circular Principal Photo Bitmap + metallic gold label: **"Principal & Standing Committee"**.
   - **Bottom-Right**: Ordered college leadership:
     1. Rector: Rev. Dr. Pavulraj Michael SJ
     2. Principal: Rev. Dr. K. Arockiam SJ
     3. Secretary: Rev. Dr. M. Arockiasamy Xavier SJ

3. **Meta Official WhatsApp Cloud API Dispatcher (`services/whatsappService.js`)**
   - Connects to Graph API v19.0 endpoint: `https://graph.facebook.com/v19.0/${PHONE_NUMBER_ID}/messages`.
   - Sends the official greeting text with attached card image link.
   - **Test & Safety Mode**: When `TEST_MODE=true`, all dispatches are safely redirected to `TEST_PHONE_NUMBER`.

4. **51-Department Code Resolver (`services/deptMapper.js`)**
   - Covers all 51 official SJC departments (`AC`, `AI`, `BI`, `BO`, `BT`, `BU`, `CB`, `CC`, `CE`, `CF`, `CH`, `CO`, `COE`, `CP`, `CR`, `CS`, `CY`, `DO`, `DS`, `EC`, `EH`, `EL`, `EN`, `ER`, `FC`, `FR`, `HI`, `HR`, `HS`, `IQ`, `JC`, `LB`, `LL`, `MA`, `ML`, `PE`, `PH`, `PO`, `S`, `S2`, `SA`, `SH`, `SO`, `SP`, `SS`, `ST`, `TA`, `VP`, `VT`, `XE`, `XX`).

---

## 🚀 Cloud Deployment Instructions

### Option 1: Deploy on Render.com (Blueprint)
1. Push this folder to a GitHub repository.
2. In Render Dashboard, click **New +** $\rightarrow$ **Blueprint**.
3. Connect your repository. Render automatically reads `render.yaml`.
4. Fill in your environment secrets in the Render Dashboard:
   - `MONGODB_URI`: Your MongoDB Atlas connection string.
   - `META_WHATSAPP_PHONE_NUMBER_ID`: Your Meta WhatsApp Phone Number ID.
   - `META_WHATSAPP_ACCESS_TOKEN`: Your Meta System User Permanent Token.
   - `SERVER_BASE_URL`: Your Render service URL (e.g., `https://sjc-birthday-backend.onrender.com`).
5. Render builds the Docker container and launches the server 24/7.

### Option 2: Deploy on Railway.app
1. Go to [railway.app](https://railway.app) $\rightarrow$ **New Project** $\rightarrow$ **Deploy from GitHub repo**.
2. Select your repository. Railway automatically detects `Dockerfile`.
3. In **Variables**, paste all variables from `.env.example`.
4. Deploy! Railway mounts the port and provides a public HTTPS domain.

### Option 3: Run with Docker Locally or on VPS
```bash
# Build the Docker image
docker build -t sjc-birthday-backend .

# Run container 24/7
docker run -d \
  --name sjc-backend \
  -p 5000:5000 \
  -e MONGODB_URI="your_mongodb_atlas_uri" \
  -e META_WHATSAPP_PHONE_NUMBER_ID="your_phone_id" \
  -e META_WHATSAPP_ACCESS_TOKEN="your_access_token" \
  -e TEST_MODE="false" \
  --restart unless-stopped \
  sjc-birthday-backend
```

---

## 📦 Seeding the 510 Faculty Records
To populate MongoDB Atlas with all 510 staff members from the official directory:

```bash
npm run seed
```
*(Upserts all 510 members and safely preserves any custom photos already uploaded by administrators)*.

---

## 🔗 API Endpoint Reference

| Method | Route | Description |
|---|---|---|
| `GET` | `/api/health` | Service uptime, MongoDB status, and Cron schedule probe |
| `GET` | `/api/faculty` | Query staff with `?search=`, `?dept=`, and `?category=` |
| `GET` | `/api/faculty/:id` | Fetch staff details by ID or MongoDB `_id` |
| `POST` | `/api/faculty/:id/photo` | One-Click Admin photo upload & MongoDB update |
| `GET` | `/api/birthdays/today` | List celebrants whose DOB matches today (Asia/Kolkata) |
| `GET` | `/api/birthdays/upcoming` | 7-day birthday forecast radar |
| `GET` | `/api/birthdays/preview/:id` | Dynamically streams 1200x675 HD PNG gift card |
| `POST` | `/api/birthdays/trigger-now` | On-demand manual trigger of daily workflow |
| `POST` | `/api/birthdays/send-single` | Send greeting to a single faculty member |
| `GET` | `/api/birthdays/logs` | Audit logs of all WhatsApp dispatches |
| `GET` | `/admin` | Web-based Faculty Manager & Photo Hub |
