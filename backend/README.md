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
