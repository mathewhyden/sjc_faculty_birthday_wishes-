# St. Joseph's College (Autonomous), Tiruchirappalli
## Faculty Birthday Automation Engine & Permanent Photo Hub (MongoDB)

This backend system provides permanent photo storage, MongoDB persistence, automated midnight cron execution (`0 1 0 * * *`), dynamic 1200x675 HD canvas greeting card generation, and automated WhatsApp delivery for all 510+ faculty and staff members of St. Joseph's College.

---

## 🏛 System Architecture & Workflow Diagram

```
                  ┌─────────────────────────────────────────┐
                  │          MONGODB ATLAS / DATABASE       │
                  │  (Stores 510+ Staff, DOBs, Photo URLs)  │
                  └────────────────────┬────────────────────┘
                                       │
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │    AUTOMATED CRON JOB SCHEDULER         │
                  │   (Runs every midnight @ 00:01 AM)      │
                  └────────────────────┬────────────────────┘
                                       │
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │     1. QUERY TODAY'S BIRTHDAYS (DD-MM)  │
                  │     2. FETCH FACULTY DETAILS & PHOTO    │
                  └────────────────────┬────────────────────┘
                                       │
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │    DYNAMIC CANVAS CARD RENDER ENGINE    │
                  │    (Generates 1200x675 PNG Image)       │
                  └────────────────────┬────────────────────┘
                                       │
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │    AUTOMATED WHATSAPP API DISPATCHER    │
                  │ (Sends Greeting Text + Card PNG Image)  │
                  └─────────────────────────────────────────┘
```

---

## 1. MongoDB Database Schema (`Faculty.js`)

Each faculty record is permanently stored with its photo URL and unique staff ID:

```javascript
const FacultySchema = new mongoose.Schema({
  facultyId: { type: String, required: true, unique: true }, // e.g. "22CPH52"
  name: { type: String, required: true },                   // e.g. "Dr. G. GENIFER SILVENA"
  deptCode: { type: String, required: true },               // e.g. "PH"
  dob: { type: String, required: true },                    // Format: "DD-MM-YYYY" (e.g. "23-09-1985")
  mobile: { type: String, required: true },                 // e.g. "9442255661"
  category: { 
    type: String, 
    enum: ['TEACHING', 'NON_TEACHING', 'JESUIT_LEADERSHIP'],
    default: 'TEACHING'
  },
  designation: { type: String, default: 'Assistant Professor' },
  photoUrl: { type: String, default: '/default-avatar.png' } // Permanent Image path/URL
}, { timestamps: true });
```

---

## 2. API Routes Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/faculty` | Retrieve all 510 staff with `?search=`, `?dept=`, and `?category=` |
| `GET` | `/api/faculty/:id` | Fetch a single staff record by `facultyId` or `_id` |
| `POST` | `/api/faculty` | Register a new faculty member |
| `PUT` | `/api/faculty/:id` | Update faculty details or photo URL |
| `POST` | `/api/faculty/upload` | Upload image file (Multer) & return saved public path |
| `POST` | `/api/faculty/:id/photo` | **Admin One-Click Photo Upload**: Saves photo to disk and updates MongoDB record |
| `GET` | `/api/birthdays/today` | Filter all staff whose birthday matches current day (DD-MM) |
| `GET` | `/api/birthdays/upcoming` | Retrieve upcoming birthdays in the next 7 days |
| `GET` | `/api/birthdays/preview/:id` | **Instant 1200x675 Card Preview**: Renders dynamic HD PNG |
| `POST` | `/api/birthdays/trigger-now` | Manually run the midnight workflow for instant testing |
| `POST` | `/api/whatsapp/send` | Send birthday message + card PNG to target staff member |
| `GET` | `/api/whatsapp/logs` | Query audit execution logs from `BirthdayLog` collection |

---

## 3. Dynamic Card Rendering Engine (`canvasService.js`)

Generates a 1200x675 HD PNG adhering strictly to the revised college format:
1. **Faculty Photo / Avatar**: Positioned on the **RIGHT** side (`X = 980px, Y = 180px, D = 170px`) with gold border.
   - If `photoUrl` is stored in MongoDB, loads and circle-clips the real photo.
   - **Fallback**: If photo is missing or fails, dynamically generates a Burgundy/Crimson gradient circle with gold monogram initials (e.g. `"GG"` for *Dr. G. Genifer Silvena*).
2. **Faculty Details**: Left-aligned (`X = 80px`).
   - Crimson Pill: `★ HAPPY BIRTHDAY PROFESSOR ★`
   - Name in bold Georgia Serif.
   - Combined single line: `"${faculty.designation} of ${getDepartmentFullName(faculty.deptCode)}"`
     *(e.g., "Assistant Professor of Department of Physics")*
3. **Bottom-Left**: Principal's Photo ($100 \times 100\text{ px}$) + Metallic Gold title: **"Principal & Standing Committee"**.
4. **Bottom-Right**: Prayers & Best Wishes with strict leadership order:
   - **Rector**: Rev. Dr. Pavulraj Michael SJ
   - **Principal**: Rev. Dr. K. Arockiam SJ
   - **Secretary**: Rev. Dr. M. Arockiasamy Xavier SJ
   - *& the entire St. Joseph's College Fraternity*

---

## 4. Automated Midnight Cron Scheduler (`cronService.js`)

The scheduler runs automatically at **00:01 AM every day (`0 1 0 * * *`)** in Asia/Kolkata timezone:
```javascript
cron.schedule('0 1 0 * * *', async () => {
  await runDailyBirthdayWorkflow('AUTOMATED_CRON');
}, {
  scheduled: true,
  timezone: 'Asia/Kolkata'
});
```

Workflow:
1. Queries MongoDB `Faculty` collection for DOB matching current date `DD-MM`.
2. Resolves department code (`PH` $\rightarrow$ `Department of Physics`).
3. Renders 1200x675 HD Card PNG via Canvas engine with stored `photoUrl`.
4. Dispatches personalized greeting text + attached card PNG via WhatsApp.
5. Writes delivery confirmation or error details to `BirthdayLog` collection.

---

## 5. 24/7 Production Deployment Guide

### Option A: Running with PM2 (Recommended for VPS / Ubuntu / Campus Server)

PM2 guarantees auto-restart on crashes and server reboots:

```bash
# 1. Install PM2 globally
npm install -g pm2

# 2. Enter backend directory and install dependencies
cd backend
npm install

# 3. Seed all 510 staff members from CSV into MongoDB Atlas
npm run seed

# 4. Start backend server with PM2
pm2 start server.js --name "sjc-birthday-backend"

# 5. Enable PM2 to restart automatically on system reboot
pm2 startup
pm2 save
```

### Option B: Running with Docker & Docker Compose

A complete `Dockerfile` and `docker-compose.yml` can be created:

```dockerfile
FROM node:18-bullseye-slim
WORKDIR /app
RUN apt-get update && apt-get install -y imagemagick libcairo2-dev libpango1.0-dev libjpeg-dev libgif-dev librsvg2-dev
COPY package*.json ./
RUN npm install --production
COPY . .
EXPOSE 5000
CMD ["node", "server.js"]
```

---

## 6. WhatsApp Session Management (Baileys / Meta Cloud API)

### WhatsApp Baileys Active Session Connectivity:
- **Persistent Session Files**: Authentication credentials (`creds.json`, app state keys) are saved in `./whatsapp-session`.
- **Auto Reconnect Loop**: Listens to `connection.update`. If the connection is dropped due to network issues, it automatically attempts reconnection with exponential backoff (`Boom.isBoom(lastDisconnect.error)`).
- **QR Code Scanning**: On initial startup, scanning the terminal or web dashboard QR code authenticates the college WhatsApp sender number permanently.

### Meta Cloud API / Webhook Integration:
- In production, set `WHATSAPP_PROVIDER=CLOUDS_API` and `WHATSAPP_API_TOKEN` in `.env`.
- Images are served as secure HTTPS URLs from `/uploads/generated_cards/` for direct attachment.
