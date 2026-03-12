# MD_02_DATN - Frontend (React + Vite)

Frontend React app cho e-commerce platform.

## Cấu trúc folder

```
frontend/
├── src/
│   ├── pages/           # Các trang (Login, Register, Home, etc.)
│   ├── components/      # Components tái sử dụng
│   ├── services/        # API services (api.js, authService.js)
│   ├── context/         # React Context (AuthContext)
│   ├── styles/          # CSS/Tailwind
│   ├── App.jsx          # Main app component
│   └── main.jsx         # Entry point
├── package.json
├── vite.config.js
├── tailwind.config.js
└── .env                 # Environment variables
```

## Cài đặt

```powershell
# Navigate to frontend folder
cd d:\DATT_HungTM19\MD_02_DATN\frontend

# Cài dependencies (đã làm)
npm install

# Copy .env.example to .env
Copy-Item .env.example -Destination .env
```

## Chạy dev server

```powershell
npm run dev
```

Server chạy tại `http://localhost:3000`

## Chạy build production

```powershell
npm run build
```

## Tính năng hiện tại

- ✅ Login / Register (JWT authentication)
- ✅ Header + Navigation
- ✅ Home page (hiển thị sản phẩm)
- ✅ AuthContext (quản lý user state)
- ✅ API service (axios + interceptors)
- ✅ Tailwind CSS styling
- ✅ Protected routes

## Tiếp theo

1. Product list & detail page
2. Cart management
3. Checkout & payment
4. Order tracking
5. User profile
6. Reviews & ratings
7. Wishlist/Favorites

## Notes

- Backend API chạy tại `http://localhost:5000`
- Vite proxy tự động forward `/api` requests tới backend
- JWT token lưu trong localStorage
