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
