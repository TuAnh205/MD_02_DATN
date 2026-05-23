# MD_02_DATN - Backend (Product API)

This folder contains a simple Express + Mongoose backend with CRUD for products.

Quick start

1. Copy `.env.example` to `.env` and set `MONGO_URI` and `PORT`.
2. Install dependencies:

```powershell
cd D:\DuAnTotNghiep\MD_02_DATN\backend
npm install
```

3. Run server:

```powershell
# Windows PowerShell
$env:MONGO_URI = 'mongodb://localhost:27017/md02_datn'; npm start
```

Auto seed on startup

The backend now auto-seeds demo data on startup by default.

- It does not delete existing data.
- It only inserts missing demo users, products, and promotions.
- Disable with `AUTO_SEED_ON_START=false` in `.env`.

Manual seed is still available:

```powershell
npm run seed
```

Team workflow for shared data
-----------------------------

Since you are the only one writing backend/frontend and another person is working on the app, remember: pulling the source code alone does not copy your runtime database data. The other developer must also use the same MongoDB data source as you.

Two valid ways to share your data:

1. Use a shared MongoDB database (recommended)
   - You keep one central DB with all current orders, users, revenue, etc.
   - Everyone sets the same `MONGO_URI` in their `.env`.
   - When the backend runs, it reads/writes to the same DB, so all devs see the same content.

2. Share a database dump / fixture snapshot
   - Export your current DB content with `mongodump` or `mongoexport`.
   - Other developers restore it to their local DB with `mongorestore` or `mongoimport`.
   - This gives them a copy of your current data.

Recommended: create a MongoDB Atlas cluster and share the `MONGO_URI` with your team (store credentials securely). Then every developer sets `MONGO_URI` in their `.env` to point to the same cluster and runs the app.

Quick commands (require MongoDB Database Tools installed):

- Create a compressed archive dump from the source machine:

```bash
mongodump --uri="$MONGO_URI" --archive=backup.gz --gzip
```

- Restore the archive on a target machine / cluster:

```bash
mongorestore --uri="$MONGO_URI" --archive=backup.gz --gzip
```

- Export a single collection to JSON (useful for fixtures):

```bash
mongoexport --uri="$MONGO_URI" --collection=products --out=fixtures/products.json --jsonArray
```

- Import a JSON fixture (upsert by `name`):

```bash
mongoimport --uri="$MONGO_URI" --collection=products --file=fixtures/products.json --jsonArray --mode=upsert --upsertFields=name
```

We added helpful npm scripts that echo the recommended commands so team members know what to run:

```powershell
npm run db:dump
npm run db:restore
npm run db:export
npm run db:import
```

Notes
- Do NOT commit credentials to git. Share `MONGO_URI` securely.
- The seed script (`npm run seed`) uses upsert (does not delete existing data) and will insert missing demo users/products/promotions.
- If you want the exact same full dataset, create a dump (`mongodump`) from the source and have others `mongorestore` it into the shared cluster or their local MongoDB.


API endpoints

- GET /api/products?limit=10&page=1&q=search
- GET /api/products/:id
- POST /api/products  (body JSON)
- PUT /api/products/:id
- DELETE /api/products/:id
- PATCH /api/products/:id/stock  (body: { stock: number })
- POST /api/products/bulk-delete (body: { ids: ["id1","id2"] })

Notes
- Ensure MongoDB is running and `MONGO_URI` points to it.
- This is a minimal scaffold. Consider adding validation, auth (JWT), file upload (multer) for images, and tests for production use.

Gmail OTP setup (important)

If register by Gmail fails with `Invalid login: 535-5.7.8 Username and Password not accepted`, your mail credentials are not valid for Gmail SMTP.

Use this config in `.env` with real values (do not keep placeholder text):

```env
MAIL_USER=your_real_gmail@gmail.com
MAIL_PASS=your_16_char_gmail_app_password
MAIL_HOST=smtp.gmail.com
MAIL_PORT=465
MAIL_SECURE=true
MAIL_FROM="MD02 DATN <your_real_gmail@gmail.com>"
```

Requirements for `MAIL_PASS`:
- You must enable 2-Step Verification on your Google account.
- Create an App Password in Google Account > Security > App passwords.
- Use the generated app password (not your normal Gmail password).
