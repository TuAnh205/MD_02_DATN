# 📱 PROJECT WORKFLOW DOCUMENTATION
## Multi-Vendor E-Commerce Platform (DATN)

---

## 📋 TÓM TẮT PROJECT

### **Mục Đích Chung**
Đây là một **nền tảng thương mại điện tử đa người bán (Multi-vendor E-commerce Platform)** hoàn chỉnh với 3 loại người dùng:
- **Người mua (User)**: Mua sắm, thanh toán, review sản phẩm
- **Người bán (Shop)**: Quản lý cửa hàng, sản phẩm, đơn hàng, doanh thu
- **Quản trị viên (Admin)**: Quản lý toàn bộ hệ thống, users, shops, orders, vouchers

---

## 🎯 CÁC FEATURES CHÍNH

### **Frontend Features:**

#### 1. **Xác thực & Tài khoản**
- Đăng nhập/Đăng ký (email/password)
- Đăng nhập với Google Firebase
- Hai loại đăng ký: người mua vs shop
- Quản lý hồ sơ cá nhân

#### 2. **Người Mua (User)**
- Browse/Tìm kiếm sản phẩm (filter, search, sort)
- Xem chi tiết sản phẩm, hình ảnh, video
- Quản lý giỏ hàng (add, update qty, remove)
- Thanh toán (COD, Momo, Zalopay, VnPay, PayPal, Stripe)
- Theo dõi đơn hàng (trạng thái, tracking)
- Viết review/đánh giá (rating 1-5, hình ảnh)
- Quản lý địa chỉ giao hàng (múltiple)
- Sử dụng voucher/khuyến mãi

#### 3. **Shop (Người Bán)**
- Dashboard tổng quan
- Quản lý sản phẩm (tạo/sửa/xóa)
- Xem đơn hàng của shop (xác nhận, ghi chú)
- Quản lý thanh toán/ví shop
- Xem doanh thu theo ngày/tháng
- Phản hồi review từ khách
- Chấp nhận chính sách phí nền tảng

#### 4. **Admin**
- Dashboard tổng quan
- Quản lý users (change role, delete)
- Quản lý shops (freeze/unfreeze, billing)
- Quản lý sản phẩm
- Quản lý đơn hàng (toàn bộ)
- Quản lý vouchers (tạo, edit, inactive)
- Quản lý bài viết/tin tức
- Xem phản hồi từ users
- Analytics doanh thu (platform, shop)

### **Backend Features:**
- API RESTful đầy đủ cho tất cả endpoints
- Xác thực JWT (JSON Web Tokens)
- Role-based access control (RBAC): user, shop, admin
- Quản lý thanh toán (tích hợp 6+ gateway)
- Hệ thống phí nền tảng cho shops (5% trên mỗi thanh toán thành công)
- Quản lý kho hàng (stock tracking)
- Hệ thống notification
- Search, filter, sort sản phẩm
- Upload hình ảnh (Cloudinary)

---

## 🔄 DATA FLOW: Từ Frontend Đến Backend

### **Ví Dụ: Quy Trình Mua Hàng**

```
FRONTEND (React)
    ↓
[User nhấn "Mua hàng"]
    ↓
API Interceptor (Frontend)
    ├─ Thêm Authorization header (Bearer Token)
    └─ Set Content-Type: application/json
    ↓
API Request (axios)
    POST /api/orders
    {
      items: [{product, price, qty, ...}],
      subtotal, total,
      payment: {method: 'momo', ...},
      shipping: {address, ...}
    }
    ↓
BACKEND (Express.js)
    ├─ Middleware: auth (verify JWT)
    ├─ Controller: orderController.createOrder()
    │   ├─ Validate dữ liệu
    │   ├─ Check sản phẩm trong DB
    │   ├─ Tính phí nền tảng (5% ngay lập tức)
    │   ├─ Save Order document
    │   └─ Tạo Notification
    └─ Response: {orderNumber, total, ...}
    ↓
FRONTEND
    └─ Hiển thị OrderSuccess page
```

### **Tương Tác API Cơ Bản:**

```javascript
// Frontend - api.js setup
const api = axios.create({
  baseURL: 'http://localhost:5000/api',
  headers: {'Content-Type': 'application/json'}
});

// Interceptor request: tự động thêm token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor response: xử lý 401 (Unauthorized)
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

---

## 🔐 Authentication Mechanism

### **1. JWT-Based Authentication**

```
Quy trình đăng nhập:
    ↓
User nhập email/password
    ↓
Backend: authController.login()
    ├─ Tìm User trong DB (email)
    ├─ So sánh password (bcryptjs)
    ├─ Generate JWT token:
    │  {id: userId, role: 'user'|'shop'|'admin'}
    ├─ JWT Secret từ environment variables
    └─ Return {user, token}
    ↓
Frontend: localStorage.setItem('token', token)
         localStorage.setItem('user', JSON.stringify(user))
    ↓
Tất cả request sau đó:
    Authorization: Bearer <token>
```

### **2. Role-Based Middleware**

```javascript
// Backend middleware/auth.js
const auth = async (req, res, next) => {
  const token = req.headers.authorization?.split(' ')[1];
  const decoded = jwt.verify(token, jwtSecret);
  req.user = {id: decoded.id, role: decoded.role};
  next();
};

const adminAuth = (req, res, next) => {
  if (req.user.role !== 'admin') 
    return res.status(403).json({message: 'Admin access required'});
  next();
};

const shopAuth = (req, res, next) => {
  if (req.user.role !== 'shop') 
    return res.status(403).json({message: 'Shop access required'});
  next();
};
```

### **3. Google OAuth Integration (Firebase)**

```
User nhấn "Đăng nhập Google"
    ↓
Frontend: firebaseAuth.signInWithGooglePopup()
    ├─ Mở Google login popup
    └─ Lấy idToken từ Firebase
    ↓
Frontend POST /api/auth/google-login
    {idToken}
    ↓
Backend: 
    ├─ Xác thực idToken (Google API)
    ├─ Tạo/Cập nhật User (firebaseUid, googleId)
    └─ Return JWT token
    ↓
Frontend: Lưu token, user, redirect home
```

---

## 📊 DATABASE MODELS (MongoDB)

### **User Collection**
```javascript
{
  _id: ObjectId,
  name: String,
  email: String (unique),
  password: String (hashed with bcrypt),
  role: 'user' | 'shop' | 'admin',
  firebaseUid: String,
  googleId: String,
  facebookId: String,
  avatar: String (URL),
  phone: String,
  addresses: [{
    street, ward, district, province,
    isDefault: Boolean
  }],
  paymentMethods: [{
    type: 'card' | 'wallet',
    details: {...}
  }],
  
  // For shops only
  shopWallet: {
    balance: Number,
    lastTopUpAt: Date
  },
  shopBillingPolicy: {
    commissionRate: 0.05,
    freeTrialDays: 3
  },
  shopStatus: 'active' | 'frozen',
  shopStatusReason: String,
  
  createdAt: Date,
  updatedAt: Date
}
```

### **Product Collection**
```javascript
{
  _id: ObjectId,
  name: String,
  price: Number,
  originalPrice: Number,
  description: String,
  detailedDescription: String,
  category: String,
  subcategory: String,
  brand: String,
  image: String (main image URL),
  images: [String] (múltiple URLs),
  videos: [String],
  stock: Number,
  variants: [{
    name: String,
    options: [String],
    priceAdjustment: Number
  }],
  attributes: {[key]: value},
  
  shopId: ObjectId (reference to User),
  shopName: String,
  
  discount: {
    type: 'percentage' | 'fixed',
    value: Number,
    startDate: Date,
    endDate: Date
  },
  
  ratings: {
    average: Number (1-5),
    count: Number
  },
  
  billing: {
    commissionRate: 0.05  // 5% phí sàn
  },
  
  isFeatured: Boolean,
  isBestSeller: Boolean,
  isNew: Boolean,
  
  createdAt: Date,
  updatedAt: Date
}
```

### **Order Collection**
```javascript
{
  _id: ObjectId,
  orderNumber: String (unique, e.g., "ORD-20240514-12345"),
  
  user: ObjectId (reference to User),
  
  items: [{
    product: ObjectId,
    productName: String,
    price: Number,
    quantity: Number,
    shopId: ObjectId,
    shopName: String,
    image: String,
    variant: String,
    platformFee: Number (tính sau)
  }],
  
  subtotal: Number,
  discount: Number (từ voucher),
  tax: Number,
  shippingFee: Number,
  total: Number,
  
  shipping: {
    address: String,
    wardId: String,
    districtId: String,
    provinceId: String,
    name: String,
    phone: String,
    trackingNumber: String
  },
  
  payment: {
    method: 'cod' | 'momo' | 'zalopay' | 'vnpay' | 'paypal' | 'stripe',
    status: 'pending' | 'completed' | 'failed',
    transactionId: String,
    paidAt: Date
  },
  
  status: 'chờ xác nhận' | 'đã xác nhận' | 'đang giao' | 'đã nhận' | 'đã hủy',
  statusHistory: [{
    status: String,
    updatedBy: String,
    reason: String,
    timestamp: Date
  }],
  
  voucher: {
    code: String,
    discount: Number
  },
  
  notes: String,
  cancelReason: String,
  
  createdAt: Date,
  updatedAt: Date
}
```

### **Cart Collection**
```javascript
{
  _id: ObjectId,
  user: ObjectId (reference to User),
  items: [{
    product: ObjectId,
    price: Number,
    quantity: Number,
    variant: String,
    image: String,
    shopId: ObjectId
  }],
  voucher: {
    code: String,
    discount: Number,
    type: 'percentage' | 'fixed'
  },
  createdAt: Date,
  updatedAt: Date
}
```

### **Review Collection**
```javascript
{
  _id: ObjectId,
  user: ObjectId,
  product: ObjectId,
  order: ObjectId,
  
  rating: Number (1-5),
  title: String,
  comment: String,
  images: [String],
  
  isVerified: Boolean (mua & đã nhận),
  
  response: {
    text: String,
    respondedAt: Date,
    respondedBy: ObjectId (shop owner)
  },
  
  createdAt: Date,
  updatedAt: Date
}
```

### **Voucher Collection**
```javascript
{
  _id: ObjectId,
  code: String (unique, uppercase),
  name: String,
  description: String,
  
  type: 'percentage' | 'fixed',
  value: Number,
  minOrderValue: Number,
  maxDiscount: Number,
  
  usageLimit: Number,
  userLimit: Number (giới hạn per user),
  used: Number,
  usedBy: [ObjectId], // track which users used
  
  applicableProducts: [ObjectId],
  applicableCategories: [String],
  
  startDate: Date,
  endDate: Date,
  isActive: Boolean,
  
  createdBy: ObjectId (admin),
  createdAt: Date
}
```

### **Notification Collection**
```javascript
{
  _id: ObjectId,
  user: ObjectId,
  
  type: 'order_status' | 'promotion' | 'review_response' | 'payment' | 'shop_status',
  
  title: String,
  message: String,
  data: {
    orderId: ObjectId,
    productId: ObjectId,
    // ... other relevant data
  },
  
  isRead: Boolean,
  readAt: Date,
  
  expiresAt: Date (auto-delete),
  
  createdAt: Date
}
```

---

## 🔄 MAIN WORKFLOWS

### **1️⃣ WORKFLOW: Đăng Ký Tài Khoản**

```
┌─────────────────────────────────────────────────┐
│ Frontend: Register.jsx                          │
├─────────────────────────────────────────────────┤
│ Step 1: Chọn loại tài khoản                     │
│         [Người mua] vs [Người bán]              │
│                                                 │
│ Step 2: Nhập thông tin                          │
│         Name, Email, Password, Confirm Password │
│         (Nếu shop: Shop name thêm)              │
│                                                 │
│ Step 3: Submit form                             │
└─────────────────────────────────────────────────┘
            ↓
POST /api/auth/register (cho user)
POST /api/auth/register-shop (cho shop)
    {name, email, password}
            ↓
┌─────────────────────────────────────────────────┐
│ Backend: authController                         │
├─────────────────────────────────────────────────┤
│ 1. Validate input (email format, pwd strength) │
│ 2. Check if email exists                       │
│ 3. Hash password với bcryptjs                  │
│ 4. Create User document                        │
│    - role: 'user' | 'shop'                     │
│    - shopWallet: {balance: 0} (nếu shop)      │
│    - shopStatus: 'active' (nếu shop)          │
│ 5. Generate JWT token                          │
│ 6. Response: {user, token}                     │
└─────────────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────────────┐
│ Frontend: Save & Navigate                       │
├─────────────────────────────────────────────────┤
│ 1. localStorage.setItem('token', token)        │
│ 2. localStorage.setItem('user', user JSON)     │
│ 3. authContext.setUser(user)                   │
│ 4. Redirect:                                   │
│    - User → /home                              │
│    - Shop → /shop/dashboard                    │
└─────────────────────────────────────────────────┘
```

### **2️⃣ WORKFLOW: Mua Sắm (Shopping)**

```
┌─────────────────────────────────────────────────┐
│ 1️⃣ DUYỆT SẢN PHẨM                              │
├─────────────────────────────────────────────────┤
Frontend: Home.jsx / Products.jsx
    └─ GET /api/products
       ?category=...&sort=...&search=...&page=1
    
Backend: productController.getProducts()
    ├─ Query MongoDB với filters
    ├─ Pagination, sort, search
    └─ Return {products: [], total}

Frontend: Display products, filters, pagination
│ 2️⃣ XEM CHI TIẾT SẢN PHẨM                       │
├─────────────────────────────────────────────────┤
Frontend: ProductDetail.jsx
    ├─ GET /api/products/:id
    └─ GET /api/reviews?productId=:id
    
Backend: 
    ├─ Lấy product details + shop info
    ├─ Lấy reviews (ratings, comments, responses)
    └─ Return {product, reviews, shop}

Frontend: Hiển thị
    ├─ Hình ảnh, video, thông tin sản phẩm
    ├─ Giá, discount, stock
    ├─ Reviews + rating trung bình
    ├─ Shop info
    └─ Nút: Thêm vào giỏ, Mua ngay

│ 3️⃣ THÊM VÀO GIỎ HÀNG                          │
├─────────────────────────────────────────────────┤
Frontend: ProductDetail.jsx
    └─ POST /api/cart
       {
         product: id,
         price: currentPrice,
         quantity: qty,
         variant: selectedVariant,
         shopId: product.shopId
       }
    
Backend: cartController.addToCart()
    ├─ Validate product & qty
    ├─ Find or Create Cart for user
    ├─ Add/Merge item (if exists, increase qty)
    ├─ Update cart total
    └─ Return updated cart

Frontend: Toast "✓ Thêm vào giỏ thành công"

│ 4️⃣ QUẢN LÝ GIỎ HÀNG                            │
├─────────────────────────────────────────────────┤
Frontend: Cart.jsx
    ├─ GET /api/cart (lấy giỏ hàng)
    ├─ PUT /api/cart/:itemId
       {quantity: newQty}
    ├─ DELETE /api/cart/:itemId (xóa item)
    └─ POST /api/cart/clear (xóa hết)

Backend: cartController
    ├─ Update quantity
    ├─ Validate stock
    ├─ Recalculate cart total
    └─ Return updated cart

Frontend: Hiển thị
    ├─ Danh sách items (product, qty, price)
    ├─ Subtotal
    ├─ Nút: Tiếp tục mua, Thanh toán
    └─ Voucher input field

│ 5️⃣ ÁP DỤNG VOUCHER                             │
├─────────────────────────────────────────────────┤
Frontend: Cart.jsx / Checkout.jsx
    └─ POST /api/cart/apply-voucher
       {code: 'SUMMER50'}
    
Backend: cartController.applyVoucher()
    ├─ Find voucher by code
    ├─ Validate:
    │  ├─ Active & not expired
    │  ├─ Min order value
    │  ├─ User limit (used count)
    │  └─ Applicable to current items
    ├─ Calculate discount
    ├─ Update cart.voucher
    └─ Return {discount, newTotal}

Frontend: Hiển thị
    ├─ Discount applied
    ├─ New total
    └─ Nút: Remove voucher

│ 6️⃣ THANH TOÁN                                  │
├─────────────────────────────────────────────────┤
Frontend: Checkout.jsx
    ├─ Nhập/Chọn địa chỉ giao hàng
    ├─ Chọn phương thức thanh toán:
    │  ├─ COD (trả tiền khi nhận)
    │  ├─ Momo
    │  ├─ Zalopay
    │  ├─ VnPay
    │  ├─ PayPal
    │  └─ Stripe
    └─ POST /api/orders
       {
         items: cart.items,
         shipping: {address, name, phone},
         payment: {method: 'momo'}, // nếu online
         subtotal, discount, tax, total
       }
    
Backend: orderController.createOrder()
    ├─ Validate data
    ├─ Check stock availability
    ├─ Calculate platform fee:
    │  ├─ Mỗi item: Fee = item.price * 0.05 (5%)
    │  ├─ Tính ngay khi tạo order
    │  └─ Lưu vào order.items[].platformFee
    ├─ Create Order document
    │  ├─ status: 'chờ xác nhận'
    │  ├─ payment.status: 'pending' (COD)
    │  ├─ order.items[].platformFee: giá trị phí cho mỗi item
    │  └─ statusHistory: [{status, timestamp}]
    ├─ Xóa items khỏi cart
    ├─ Tạo Notification cho user
    ├─ (Shop không được notification, shop thấy ở orders page)
    └─ Return {orderNumber, orderId, total}

Frontend: Redirect to OrderSuccess.jsx
    ├─ Hiển thị orderNumber
    ├─ Thông tin đơn hàng
    ├─ Trạng thái: "Chờ xác nhận"
    └─ Nút: Continue shopping, View order

│ 7️⃣ THANH TOÁN ONLINE (tùy chọn)               │
├─────────────────────────────────────────────────┤
Frontend: Order detail page
    └─ Nếu payment.status = 'pending' & method ≠ 'cod'
    └─ POST /api/orders/:id/process-payment
       {method: 'momo', amountNeeded: total}
    
Backend: 
    ├─ Call payment gateway (Momo SDK, etc)
    ├─ Return {paymentUrl} hoặc {paymentToken}
    └─ Frontend redirect to payment gateway

Payment Gateway:
    ├─ User nhập thông tin thanh toán
    ├─ Xác nhận & trả về transactionId
    └─ Callback to backend

Backend (Webhook):
    ├─ Verify callback signature
    ├─ Update order.payment:
    │  ├─ status: 'completed'
    │  ├─ transactionId
    │  ├─ paidAt: now
    │  └─ statusHistory.push({...})
    ├─ Create Notification
    └─ (Shop thấy order này có payment completed)

Frontend: 
    ├─ Polling atau Socket.io listen payment status
    ├─ Khi success:
    │  ├─ Redirect to OrderSuccess
    │  └─ Display success message
    └─ Khi fail: Display error & offer retry

┌──────────────────────────────────────────────────┐
│ 📊 SUMMARY: QUY TRÌNH MUA HÀNG                   │
├──────────────────────────────────────────────────┤
│ Browse → Detail → Add Cart → Checkout →          │
│ Payment (if online) → Order Created →            │
│ Shop xác nhận → Ship → Delivery → User confirm → │
│ Có thể Review                                     │
└──────────────────────────────────────────────────┘
```

### **3️⃣ WORKFLOW: Quản Lý Đơn Hàng (Orders)**

```
┌──────── PHÍA NGƯỜI MUA (USER) ─────────┐
├────────────────────────────────────────┤
Frontend: Orders.jsx
    └─ GET /api/orders
       (mỗi user chỉ thấy orders của mình)
    
Backend: orderController.getOrders()
    ├─ Filter: orders where user = req.user.id
    ├─ Sort by createdAt desc
    └─ Return orders[]

Frontend: Hiển thị danh sách đơn hàng
    ├─ Order number
    ├─ Tổng tiền
    ├─ Trạng thái:
    │  ├─ ⏳ Chờ xác nhận (chưa ship)
    │  ├─ ✓ Đã xác nhận (chờ ship)
    │  ├─ 🚚 Đang giao (đang vận chuyển)
    │  ├─ 📦 Đã nhận (giao thành công)
    │  └─ ❌ Đã hủy
    └─ Nút: Xem chi tiết, Hủy đơn (nếu chưa ship)

User nhấn "Xem chi tiết"
    └─ GET /api/orders/:id
    
Backend: orderController.getOrderDetail()
    ├─ Validate: user owns this order
    ├─ Return order với tất cả details
    └─ Include: items, payment, shipping, tracking

Frontend: Order detail page
    ├─ Product list (name, qty, price)
    ├─ Subtotal, discount, total
    ├─ Shipping address & tracking number
    ├─ Payment method & status
    ├─ Status timeline (xác nhận → giao → nhận)
    ├─ Nút:
    │  ├─ Hủy (nếu chờ xác nhận)
    │  ├─ Thanh toán online (nếu COD & chưa thanh toán)
    │  ├─ Confirm received (nếu đang giao)
    │  └─ Review (nếu đã nhận)
    └─ Chat với shop (nếu có)

User nhấn "Hủy đơn hàng"
    └─ PATCH /api/orders/:id/cancel
       {reason: 'Thay đổi ý định' | 'Sản phẩm không như mô tả' | ...}
    
Backend: orderController.cancelOrder()
    ├─ Validate: order status must be 'chờ xác nhận'
    ├─ Update order:
    │  ├─ status: 'đã hủy'
    │  ├─ cancelReason: reason
    │  └─ statusHistory.push({status, reason, timestamp})
    ├─ Refund payment:
    │  ├─ Nếu đã thanh toán online
    │  └─ Call payment gateway refund API
    ├─ Restore product stock
    └─ Create Notification

Frontend: Success toast, order status updated

User nhấn "Confirm received"
    └─ PATCH /api/orders/:id/status
       {status: 'đã nhận'}
    
Backend:
    ├─ Validate: status is 'đang giao'
    ├─ Update status: 'đã nhận'
    ├─ Shop wallet: cộng số tiền (trừ phí)
    ├─ statusHistory.push({...})
    └─ Create Notification (user can now review)

┌──────── PHÍA SHOP (SELLER) ─────────┐
├────────────────────────────────────────┤
Frontend: ShopOrders.jsx
    └─ GET /api/shop/orders
    
Backend: shopController.getShopOrders()
    ├─ Filter: orders có items từ shop này
    ├─ Return orders[]
    └─ (Chỉ show orders của shop, không show từ shops khác)

Frontend: Shop xem đơn hàng
    ├─ Order ID
    ├─ Customer name, address
    ├─ Items (chỉ items từ shop này)
    ├─ Total tiền từ items này
    ├─ Status
    ├─ Payment status
    └─ Nút: Xác nhận, Từ chối, Add tracking, Ghi chú

Shop nhấn "Xác nhận đơn"
    └─ PATCH /api/shop/orders/:id/status
       {status: 'đã xác nhận'}
    
Backend: orderController.updateOrderStatus()
    ├─ Validate: user is shop owner
    ├─ Check: order status is 'chờ xác nhận'
    ├─ Update order.status: 'đã xác nhận'
    ├─ statusHistory.push({...})
    ├─ Create Notification cho user: "Shop đã xác nhận đơn"
    └─ Return updated order

Frontend: Order status updated, show ship form

Shop nhấn "Giao hàng"
    └─ PATCH /api/shop/orders/:id/status
       {
         status: 'đang giao',
         trackingNumber: '123456789'
       }
    
Backend:
    ├─ Update order.shipping.trackingNumber
    ├─ Update order.status: 'đang giao'
    ├─ statusHistory.push({...})
    └─ Create Notification: "Đơn hàng đang giao, tracking: ..."

User nhận được notification
    ├─ Có thể track delivery
    └─ Khi nhận xong → Confirm received

┌──────── PHÍA ADMIN ─────────┐
├────────────────────────────────────────┤
Frontend: AdminOrders.jsx
    └─ GET /api/admin/orders
    
Backend: adminController.getOrders()
    ├─ Query ALL orders (no filter on user/shop)
    ├─ Pagination, sort
    └─ Return all orders[]

Frontend: Admin xem tất cả orders
    ├─ Danh sách đầy đủ
    ├─ Filter: by user, by shop, by status, by date
    ├─ Analytics: total revenue, etc
    └─ Nút: View detail, Force cancel (nếu cần)

Admin xem chi tiết order
    ├─ Tất cả thông tin
    ├─ Platform fee details (mỗi item: 5%)
    ├─ Payment status
    ├─ User & shop info
    └─ Có thể update status if issues (rare)
```

### **4️⃣ WORKFLOW: Quản Lý Shop (Người Bán)**

```
┌─── ĐĂNG KÝ SHOP ───┐
├──────────────────────────────┤
User chọn "Đăng ký là người bán"
    └─ POST /api/auth/register-shop
       {name, email, password, shopName}
    
Backend:
    ├─ Create User (role: 'shop')
    ├─ shopStatus: 'active'
    ├─ shopWallet: {balance: 0}
    ├─ shopBillingPolicy: {accepted: false}
    └─ Return {user, token}

Frontend: Redirect to ShopDashboard
    
│ ✅ ACCEPT POLICY │
├──────────────────────────────┤
Frontend: ShopDashboard.jsx
    ├─ Show dialog: Chính sách phí nền tảng
    │  ├─ Phí 5% cho mỗi sản phẩm bán được
    │  ├─ Phí được tính ngay khi thanh toán thành công
    │  ├─ Ví dụ: Bán 100.000đ → Phí sàn 5.000đ → Thực nhận 95.000đ
    │  └─ Phí: Trừ trực tiếp vào tiền nhận, không từ khách
    └─ Button: "Chấp nhận" | "Từ chối"
    
Backend: POST /api/shop/billing-policy/accept
    ├─ Update user.shopBillingPolicy.accepted: true
    ├─ Update user.shopBillingPolicy.acceptedAt: now
    └─ Unlock: shop có thể đăng sản phẩm

│ 📤 ĐĂNG SẢN PHẨM │
├──────────────────────────────┤
Frontend: ShopProducts.jsx → "Thêm sản phẩm"
    ├─ Form:
    │  ├─ Name, price, original price
    │  ├─ Category, brand, sku
    │  ├─ Description (text + rich editor)
    │  ├─ Images (upload 1-n hình)
    │  ├─ Variants (size, color, etc)
    │  ├─ Stock qty
    │  ├─ Discount (optional)
    │  └─ Submit
    └─ POST /api/shop/products
       {name, price, category, image, stock, ...}
    
Backend: shopController.createProduct()
    ├─ Validate input
    ├─ Check shopBillingPolicy.accepted
    ├─ Create Product document
    ├─ Set billing: {
    │    commissionRate: 0.05,  // 5% phí sàn
    │    appliedImmediately: true
    │  }
    ├─ shopId: userId
    ├─ images: upload to Cloudinary
    └─ Return product

Frontend: "Sản phẩm được thêm thành công"
    
    📊 TÍNH PHÍ 5% 📊
    ┌──────────────────────────────────────────┐
    │ Khi thanh toán thành công:               │
    │ Tính 5% phí sàn ngay lập tức             │
    │                                          │
    │ Ví dụ: Sản phẩm 100.000đ bán được 1:    │
    │ ├─ Giá bán: 100.000đ                     │
    │ ├─ Phí sàn (5%): 5.000đ                  │
    │ └─ Tiền thực nhận: 95.000đ               │
    │                                          │
    │ Tiền vào wallet ngay:                    │
    │ └─ +95.000đ (đã trừ 5.000đ phí)          │
    └──────────────────────────────────────────┘

│ 📋 QUẢN LÝ SẢN PHẨM │
├──────────────────────────────┤
Frontend: ShopProducts.jsx
    ├─ GET /api/shop/products
    ├─ Danh sách products của shop
    ├─ Mỗi product:
    │  ├─ Hình, tên, giá
    │  ├─ Stock
    │  ├─ Ngày tạo
    │  └─ Nút: Sửa, Xóa, Ẩn/Hiện
    └─ Nút: Thêm sản phẩm

Shop nhấn "Sửa sản phẩm"
    └─ PUT /api/shop/products/:id
       {name, price, stock, ...}
    
Backend: shopController.updateProduct()
    ├─ Validate: shop owns product
    ├─ Update product fields
    └─ Return updated product

Shop nhấn "Xóa sản phẩm"
    └─ DELETE /api/shop/products/:id
    
Backend:
    ├─ Validate: shop owns product
    ├─ Delete product
    ├─ Remove from all carts
    └─ Return status

│ 💰 QUẢN LÝ PAYMENT │
├──────────────────────────────┤
Khi customer thanh toán thành công:
    ├─ Order created
    ├─ Tính phí ngay lập tức:
    │  ├─ Mỗi item: Fee = item.price * 0.05 (5%)
    │  ├─ Revenue = item.price - fee
    │  └─ Lưu vào database (order.items[].platformFee)
    └─ Shop nhận tiền vào wallet:
       ├─ Wallet += net_revenue (đã trừ phí)
       └─ Notification: "Thanh toán từ sản phẩm X: +95.000đ (phí sàn: 5.000đ)"

Frontend: ShopRevenue.jsx
    ├─ GET /api/shop/revenue
    └─ Hiển thị chi tiết:
       ├─ 📊 Tổng tiền bán được (gross revenue)
       ├─ 📋 Chi tiết từng sản phẩm:
       │  ├─ Tên sản phẩm
       │  ├─ Giá bán
       │  ├─ Số lượng bán
       │  ├─ Tổng tiền (giá × qty)
       │  ├─ Phí sàn (5%)
       │  └─ Tiền thực nhận (tổng - phí)
       ├─ 💰 Tổng tiền thực nhận (net revenue)
       ├─ 📈 Chart: doanh thu theo ngày/tháng
       └─ 💳 Wallet balance (tiền sẵn có)

│ 💳 RÚT TIỀN │
├──────────────────────────────┤
Frontend: ShopDashboard
    └─ Button: "Rút tiền"
    └─ POST /api/shop/wallet/withdraw
       {amount: 100000, bankAccount: {...}}
    
Backend:
    ├─ Validate amount <= wallet.balance
    ├─ Create withdrawal transaction
    ├─ Update wallet.balance
    ├─ Transfer tiền to shop's bank account
    └─ Notification: "Rút tiền thành công: 100.000đ"

Shop ngân hàng:
    ├─ Tiền vào tài khoản trong 1-3 ngày làm việc
    └─ Email xác nhận giao dịch

Frontend: Notification & status updated
```

### **5️⃣ WORKFLOW: Review & Rating**

```
┌─ ĐIỀU KIỆN VIẾT REVIEW ─┐
├──────────────────────────────┤
User có thể viết review khi:
    ✓ Đã mua sản phẩm
    ✓ Order đã được confirm "đã nhận"
    ✓ Chưa viết review cho order này

Frontend: OrderDetail page
    └─ Nếu order.status = 'đã nhận'
    └─ Hiển thị "Viết review" button

│ VIẾT REVIEW │
├──────────────────────────────┤
User nhấn "Viết review"
    └─ Mở review modal/page
    └─ Form:
       ├─ Rating (1-5 stars)
       ├─ Title
       ├─ Comment (tối thiểu n ký tự)
       ├─ Images (upload optional)
       └─ Submit

Frontend: POST /api/reviews
    {
      product: productId,
      order: orderId,
      rating: 5,
      title: "Sản phẩm tốt",
      comment: "Chất lượng cao...",
      images: [urls]
    }
    
Backend: reviewController.createReview()
    ├─ Validate:
    │  ├─ User owns order
    │  ├─ Order.product = reviewProduct
    │  ├─ Order.status = 'đã nhận'
    │  └─ Not already reviewed
    ├─ Create Review document
    ├─ Mark isVerified: true (đã mua & nhận)
    ├─ Update Product.ratings:
    │  ├─ ratings.count += 1
    │  └─ ratings.average = recalculate
    ├─ Create Notification for shop:
    │  └─ "Bạn nhận được 1 review từ khách"
    └─ Return review

Frontend: "Review được thêm thành công"

│ SHOP PHẢN HỒI REVIEW │
├──────────────────────────────┤
Shop nhận notification
    └─ Frontend: ShopReviews.jsx
    └─ GET /api/shop/reviews
    
Backend: shopController.getShopReviews()
    ├─ Get all reviews for shop's products
    ├─ Sort by newest
    └─ Return reviews[]

Frontend: Hiển thị reviews
    ├─ Product name
    ├─ Rating & comment
    ├─ Images
    ├─ User name
    ├─ Review date
    └─ Nút: "Phản hồi" (nếu chưa reply)

Shop nhấn "Phản hồi"
    └─ PUT /api/shop/reviews/:id/reply
       {
         text: "Cảm ơn quý khách. " +
               "Chúng tôi sẽ cải thiện hơn..."
       }
    
Backend: reviewController.addResponse()
    ├─ Add response to Review:
    │  ├─ response.text
    │  ├─ response.respondedAt: now
    │  └─ response.respondedBy: shopId
    ├─ Update review
    ├─ Create Notification for customer:
    │  └─ "Shop đã phản hồi review của bạn"
    └─ Return updated review

Frontend: 
    ├─ Hiển thị shop response
    ├─ Khi user view review: thấy cả comment + reply
    └─ When user get notification: navigate to review

│ USER XEM REVIEW │
├──────────────────────────────┤
Frontend: ProductDetail.jsx
    └─ GET /api/reviews?productId=:id
    
Backend: Lấy reviews của product
    ├─ Filter: isVerified = true (đã mua)
    ├─ Sort: helpful, newest
    ├─ Pagination
    └─ Return reviews[]

Frontend: Reviews section on product page
    ├─ Rating distribution (5★: 10, 4★: 5, ...)
    ├─ Average rating
    ├─ Reviews list:
    │  ├─ "Verified" badge
    │  ├─ User name (có thể ẩn)
    │  ├─ Rating & comment
    │  ├─ Images
    │  ├─ Shop reply (if exists)
    │  └─ Helpful button
    └─ Filter/Sort option
```

### **6️⃣ WORKFLOW: Quản Lý Admin**

```
┌─ QUẢN LÝ USERS ─┐
├──────────────────────────────┤
Frontend: AdminUsers.jsx
    └─ GET /api/admin/users
    
Backend: adminController.getUsers()
    ├─ Query all users
    ├─ Pagination
    ├─ Filter by role: user, shop, admin
    └─ Return users[]

Frontend: Users table
    ├─ Email
    ├─ Role
    ├─ Status (active/frozen - for shops)
    ├─ Created date
    ├─ Updated date
    └─ Nút: View, Change role, Delete

Admin nhấn "Change role"
    └─ PUT /api/admin/users/:id/role
       {role: 'admin'} hoặc {role: 'shop'}
    
Backend:
    ├─ Update user.role
    ├─ If role = shop:
    │  └─ Initialize shopWallet, billing policy
    └─ Return updated user

Admin nhấn "Delete user"
    └─ DELETE /api/admin/users/:id
    
Backend: adminController.deleteUser()
    ├─ Validate: cannot delete self (current admin)
    ├─ Delete user
    ├─ Delete related:
    │  ├─ Carts
    │  ├─ Orders (hoặc set to anonymous)
    │  ├─ Reviews
    │  ├─ Products (if shop)
    │  └─ Addresses
    └─ Return status

Frontend: User removed from list

┌─ QUẢN LÝ SHOPS ─┐
├──────────────────────────────┤
Frontend: AdminShops.jsx
    └─ GET /api/admin/shops
    
Backend: adminController.getShops()
    ├─ Query users where role = 'shop'
    ├─ Return shops with billing info
    └─ shops[]

Frontend: Shops table
    ├─ Shop name (email)
    ├─ Wallet balance
    ├─ Status (active/frozen)
    ├─ Outstanding amount
    ├─ Created date
    └─ Nút: View, Freeze, Unfreeze

Admin nhấn "View shop detail"
    ├─ GET /api/admin/shops/:id
    
Backend: Return shop info + products + revenue

Frontend: Shop detail page
    ├─ Tất cả thông tin shop
    ├─ Products list
    ├─ Recent orders
    ├─ Billing summary
    └─ Nút: Freeze, Unfreeze

Admin nhấn "Freeze shop"
    └─ PUT /api/admin/shops/:id/status
       {status: 'frozen', reason: 'Bán hàng fake'}
    
Backend: adminController.freezeShop()
    ├─ Update user.shopStatus: 'frozen'
    ├─ Update shopStatusReason
    ├─ Update all products:
    │  └─ Cannot be purchased
    ├─ Create Notification for shop owner:
    │  └─ "Shop của bạn đã bị khóa vì: ..."
    └─ Email to shop

Frontend: Status updated

Admin nhấn "Unfreeze shop"
    └─ PUT /api/admin/shops/:id/status
       {status: 'active'}
    
Backend:
    ├─ Update shopStatus: 'active'
    ├─ Products available again
    ├─ Create Notification
    └─ Email to shop

┌─ QUẢN LÝ VOUCHERS ─┐
├──────────────────────────────┤
Frontend: AdminVouchers.jsx
    └─ GET /api/admin/vouchers
    
Backend: Return all vouchers

Frontend: Vouchers table
    ├─ Code
    ├─ Type & value (50%, 10.000đ)
    ├─ Valid date
    ├─ Usage (10/100)
    ├─ Status (active/inactive)
    └─ Nút: View, Edit, Delete, Deactivate

Admin nhấn "Create voucher"
    └─ Form:
       ├─ Code (SUMMER50)
       ├─ Name
       ├─ Description
       ├─ Type: percentage / fixed
       ├─ Value
       ├─ Min order value
       ├─ Max discount
       ├─ Usage limit
       ├─ Per user limit
       ├─ Date range
       ├─ Applicable to:
       │  ├─ All products
       │  ├─ Selected categories
       │  └─ Selected products
       └─ Submit

Frontend: POST /api/admin/vouchers
    {
      code: 'SUMMER50',
      type: 'percentage',
      value: 50,
      minOrderValue: 100000,
      maxDiscount: 50000,
      usageLimit: 100,
      userLimit: 2,
      startDate, endDate,
      applicableCategories: ['Áo', 'Quần']
    }
    
Backend: adminController.createVoucher()
    ├─ Validate:
    │  ├─ Unique code
    │  ├─ Valid value range
    │  └─ Date range
    ├─ Create Voucher document
    └─ Return voucher

Frontend: "Voucher created successfully"

Admin nhấn "Edit voucher"
    └─ PUT /api/admin/vouchers/:id
       {...updates}
    
Backend: Update voucher

Admin nhấn "Deactivate"
    └─ PATCH /api/admin/vouchers/:id
       {isActive: false}
    
Backend:
    ├─ Update isActive
    ├─ Customers cannot use this code anymore
    └─ Return status

┌─ ANALYTICS & REVENUE ─┐
├──────────────────────────────┤
Frontend: AdminRevenue.jsx
    └─ GET /api/admin/revenue?from=date&to=date
    
Backend: adminController.getRevenue()
    ├─ Query orders in date range
    ├─ Calculate:
    │  ├─ Total order value (Σ total)
    │  ├─ Platform fee collected (Σ fee)
    │  ├─ Refunds (Σ refund)
    │  └─ Net revenue
    ├─ Top products
    ├─ Top shops
    ├─ Top categories
    └─ Return analytics data

Frontend: Dashboard with charts
    ├─ Revenue overview (cards)
    ├─ Revenue over time (line chart)
    ├─ Top products (bar chart)
    ├─ Top shops (table)
    ├─ Top categories (pie chart)
    └─ Export button (PDF/CSV)

Admin can filter:
    ├─ By date range
    ├─ By shop
    ├─ By product category
    └─ By payment method
```

---

## 📡 API ENDPOINTS SUMMARY

### **Authentication**
```
POST   /api/auth/register           → Đăng ký người mua
POST   /api/auth/register-shop      → Đăng ký người bán
POST   /api/auth/login              → Đăng nhập
POST   /api/auth/google-login       → Đăng nhập Google
POST   /api/auth/forgot-password    → Quên mật khẩu
POST   /api/auth/reset-password     → Reset mật khẩu
GET    /api/auth/me                 → Lấy profile hiện tại
PUT    /api/auth/me                 → Cập nhật profile
```

### **Products**
```
GET    /api/products                → Lấy danh sách sản phẩm (filter, search)
GET    /api/products/:id            → Chi tiết sản phẩm
POST   /api/shop/products           → Tạo sản phẩm (Shop)
PUT    /api/shop/products/:id       → Cập nhật sản phẩm (Shop)
DELETE /api/shop/products/:id       → Xóa sản phẩm (Shop)
GET    /api/shop/products           → Danh sách sản phẩm của shop
```

### **Cart**
```
GET    /api/cart                    → Lấy giỏ hàng
POST   /api/cart                    → Thêm item vào giỏ
PUT    /api/cart/:itemId            → Cập nhật số lượng
DELETE /api/cart/:itemId            → Xóa item
POST   /api/cart/clear              → Xóa toàn bộ giỏ
POST   /api/cart/apply-voucher      → Áp dụng mã voucher
DELETE /api/cart/voucher            → Bỏ voucher
```

### **Orders**
```
POST   /api/orders                  → Tạo đơn hàng (User)
GET    /api/orders                  → Danh sách đơn hàng (User)
GET    /api/orders/:id              → Chi tiết đơn hàng (User)
POST   /api/orders/:id/process-payment  → Thanh toán online
PATCH  /api/orders/:id/status       → Update trạng thái
PATCH  /api/orders/:id/cancel       → Hủy đơn hàng
GET    /api/shop/orders             → Danh sách đơn của shop
GET    /api/admin/orders            → Danh sách tất cả đơn (Admin)
```

### **Reviews**
```
POST   /api/reviews                 → Tạo review
GET    /api/reviews?productId=X     → Lấy reviews theo product
PUT    /api/reviews/:id             → Cập nhật review (User)
DELETE /api/reviews/:id             → Xóa review (User)
PUT    /api/reviews/:id/reply       → Reply review (Shop)
GET    /api/shop/reviews            → Danh sách reviews cho shop
GET    /api/admin/reviews           → Danh sách tất cả reviews (Admin)
```

### **Shop Management**
```
GET    /api/shop/revenue            → Doanh thu cửa hàng (gross + chi tiết phí + net)
GET    /api/shop/revenue/:productId → Chi tiết doanh thu từng sản phẩm (giá, phí, thực nhận)
GET    /api/shop/wallet             → Số dư ví & lịch sử giao dịch
POST   /api/shop/wallet/withdraw    → Rút tiền từ ví
POST   /api/shop/billing-policy/accept  → Chấp nhận chính sách phí
GET    /api/shop/products           → Sản phẩm của shop
GET    /api/shop/transactions       → Lịch sử giao dịch chi tiết (in/out)
```

### **Admin Management**
```
GET    /api/admin/users             → Danh sách users
PUT    /api/admin/users/:id/role    → Thay đổi role user
DELETE /api/admin/users/:id         → Xóa user
GET    /api/admin/shops             → Danh sách shops
PUT    /api/admin/shops/:id/status  → Khóa/Mở shop
GET    /api/admin/orders            → Tất cả đơn hàng
GET    /api/admin/revenue           → Analytics doanh thu
GET    /api/admin/vouchers          → Danh sách vouchers
POST   /api/admin/vouchers          → Tạo voucher
PUT    /api/admin/vouchers/:id      → Cập nhật voucher
DELETE /api/admin/vouchers/:id      → Xóa voucher
GET    /api/admin/reviews           → Danh sách reviews
GET    /api/admin/feedbacks         → Phản hồi từ users
```

---

## 🔗 Technology Stack

### **Frontend:**
- **React 18** - UI Library
- **React Router v6** - Client-side routing
- **Axios** - HTTP client with interceptors
- **Tailwind CSS** - Utility-first CSS framework
- **Vite** - Fast build tool
- **Firebase SDK** - Google authentication
- **State Management** - localStorage + React Context (AuthContext)

### **Backend:**
- **Node.js + Express.js** - Web server framework
- **MongoDB + Mongoose** - NoSQL database & ODM
- **JWT (jsonwebtoken)** - Token-based authentication
- **Bcryptjs** - Password hashing
- **Passport.js** - OAuth strategies
- **Payment Gateways:**
  - Momo
  - Zalopay
  - VnPay
  - PayPal
  - Stripe
- **Cloudinary** - Image upload & CDN
- **Nodemailer** - Email service
- **Socket.io** - Real-time notifications (optional)

---

## 📝 Tóm Tắt Luồng Dữ Liệu Chính

### **1. User Shopping Flow**
```
Register → Login → Browse Products → 
Add to Cart → Apply Voucher → Checkout → 
Payment → Order Created → Ship → Delivery → 
Confirm Received → Write Review → 
Shop Replies Review
```

### **2. Shop Management Flow**
```
Register (as shop) → Accept Billing Policy → 
Upload Products → Manage Inventory → 
View Orders → Confirm & Ship → 
Track Wallet/Revenue → Pay Platform Fees
```

### **3. Admin Oversight Flow**
```
Dashboard → Monitor Users/Shops/Orders → 
Create Promotions → Handle Disputes → 
View Analytics → Manage System
```

---

## 🔄 Key Business Logic

### **Platform Fee System**
- **Commission:** 5% từ giá sản phẩm, tính **ngay khi thanh toán thành công** (không có giai đoạn miễn phí)
- **Instant Deduction:** Phí trừ trực tiếp, shop chỉ nhận tiền thực (đã trừ phí)
- **Transaction-Level Tracking:** Mỗi giao dịch hiển thị:
  - Giá sản phẩm: 100.000đ
  - Phí sàn (5%): 5.000đ
  - Tiền thực nhận: 95.000đ
- **Revenue Dashboard:** Shop xem chi tiết phí từng sản phẩm trong quản lý doanh thu
  - Tổng tiền bán được (gross)
  - Chi tiết phí từng sản phẩm
  - Tổng tiền thực nhận (net)
- **Wallet Management:** Tiền vào wallet ngay sau khi trừ phí, shop có thể rút ra hoặc sử dụng

### **Order Status & Payment Flow**
```
1. Order created (thanh toán thành công)
   ├─ Fee tính ngay: 5% × giá sản phẩm
   ├─ Shop wallet += tiền thực nhận (đã trừ phí)
   └─ Notification: "Bạn nhận +X đ, phí sàn: Y đ"

2. Order status flow:
   chờ xác nhận → đã xác nhận → đang giao → đã nhận (→ có thể review)
   ↓
   đã hủy (nếu customer hủy)

3. Refund (nếu hủy):
   ├─ Refund to customer
   ├─ Deduct từ shop wallet (dù shop chưa rút)
   └─ Notification: "Đơn hủy, hoàn lại -X đ"
```

### **Payment Methods**
- **COD (Cash on Delivery):** Thanh toán khi nhận hàng
- **Online Gateways:** Momo, Zalopay, VnPay, PayPal, Stripe

### **Voucher System**
- Loại: % giảm hoặc số tiền cố định
- Điều kiện: Min order value, applicable products/categories
- Giới hạn: Usage limit, per-user limit
- Thời hạn: Start date - End date

---

## 🎯 Key Features Summary

| Feature | User | Shop | Admin |
|---------|------|------|-------|
| Browse & Buy | ✅ | - | - |
| Cart Management | ✅ | - | - |
| Order Tracking | ✅ | ✅ | ✅ |
| Review Products | ✅ | ✅ | ✅ |
| Manage Inventory | - | ✅ | - |
| Track Revenue | - | ✅ | ✅ |
| Manage Users | - | - | ✅ |
| Manage Shops | - | - | ✅ |
| Create Promotions | - | - | ✅ |
| View Analytics | - | ✅ | ✅ |
| Platform Fees | - | ✅ | ✅ |

---

**Generated:** May 14, 2026  
**Project Type:** Multi-Vendor E-Commerce Platform  
**Last Updated:** Based on comprehensive codebase analysis
