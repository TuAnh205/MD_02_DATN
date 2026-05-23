# Backend Overview - MD_02_DATN E-Commerce Platform

## 🏗️ Architecture

### Tech Stack

- **Framework**: Express.js (Node.js)
- **Database**: MongoDB (Mongoose ODM)
- **Authentication**: JWT + Google OAuth + Firebase
- **Email**: Nodemailer (Gmail SMTP)
- **Payment**: Stripe, PayPal, Momo, ZaloPay, VNPay
- **Real-time**: Socket.io
- **Cloud Storage**: Cloudinary
- **Task Scheduling**: node-cron

---

## 📂 Project Structure

```
backend/
├── app.js                 # Main server file
├── package.json          # Dependencies
├── config/               # Configuration files
│   ├── db.js            # MongoDB connection
│   └── shopBillingPolicy.js  # Platform fee configuration
├── controllers/          # Business logic (14 files)
├── models/              # Mongoose schemas (13 models)
├── routes/              # API endpoints (15 route files)
├── middleware/          # Express middleware
│   └── auth.js         # JWT & role-based auth
├── services/            # Service layer
│   └── shopBillingService.js  # Shop billing logic
├── jobs/               # Scheduled jobs
│   ├── scheduler.js    # Cron job runner
│   └── voucherExpiryNotifier.js  # Expiry notifications
├── seeds/              # Database seeding
└── data/               # Static data (Vietnam provinces)
```

---

## 🗄️ Database Models

### 1. **User**

- Authentication & Profile
- Email (unique, indexed)
- Password (bcrypt hashed)
- Firebase UID
- Google/Facebook IDs
- User roles: `user`, `shop`, `admin`
- Addresses (array)
- Payment methods (array)
- Preferences (notifications)
- Shop Wallet (balance, transaction history)
- Shop Billing Summary (outstanding amount, payment status)
- Shop Status: `active` or `frozen`

### 2. **Product**

- E-commerce product
- Name, SKU (unique), description
- Price, original price, discount
- Stock management
- Category, subcategory, brand
- Images & videos (array)
- Variants (color, size, etc.)
- Ratings (average, count)
- Tags & SEO
- Shop association (shopId, createdBy)
- Billing info (commission rate, fee status)
- Promotion references

### 3. **Order**

- Order number (ORD-{timestamp}-{random})
- User association
- Order items (array)
- Subtotal, discount, tax, total
- Shipping address & method
- Payment info (method, status, transaction ID)
- Order status: `chờ xác nhận`, `đã xác nhận`, `đang giao`, `đã nhận`, `đã hủy`, `trả hàng`, `hoàn tiền`
- Status history with timestamps
- Platform fee tracking per item

### 4. **Cart**

- User association (unique)
- Cart items (product, price, qty, variant)
- Voucher code & discount applied

### 5. **Review**

- Product reviews
- User association
- Rating (1-5 stars)
- Comment with images
- Helpful count
- Shop response capability
- Verified purchase flag

### 6. **Voucher**

- Code (unique, uppercase)
- Type: `percentage` or `fixed`
- Min order value
- Usage limits (total & per user)
- Applicable products/categories
- Date range (startDate, endDate)
- Active flag

### 7. **Promotion**

- Admin-created promotions
- Type: `percentage`, `fixed`, `buy_x_get_y`, `free_shipping`
- Conditions (min order, applicable items)
- Usage limits
- Date range
- Discount code

### 8. **Notification**

- User association
- Type: `order_status`, `shop_order`, `promotion`, `voucher`, `review`, `review_response`, `system`
- Title & message
- Data payload
- Read status with timestamp
- Auto-expires

### 9. **Post**

- Blog post
- Title, content
- Author, published status

### 10. **Favorite**

- User-Product association
- Unique constraint on (user, product)

### 11. **Feedback**

- User feedback/contact
- Message, contact info
- Timestamp

### 12. **Banner**

- Homepage banners
- Position/ordering
- Link type: `product`, `category`, `external`, `none`
- Click tracking
- Date range

### 13. **Order Item Schema** (nested in Order)

- Product reference
- Shop reference
- Name, SKU, price
- Quantity
- Platform fee tracking (rate, amount, status, charge date)

---

## 🔐 Authentication & Authorization

### Middleware: `auth.js`

#### 1. **auth** - JWT Verification

- Extracts Bearer token from Authorization header
- Verifies JWT signature
- Attaches `req.user = { id, role }` to request
- Returns 401 if invalid/missing

#### 2. **adminAuth** - Admin Only

- Checks if user role is `admin`
- Returns 403 if not admin

#### 3. **shopAuth** - Shop Only

- Checks if user role is `shop`
- Calls `syncShopBilling()` to update shop state
- Attaches shop info to `req.shop`
- Returns 403 if not shop

#### 4. **ensureShopCanSell** - Shop Not Frozen

- Checks if shop is frozen due to unpaid platform fees
- Returns 403 with billing summary if frozen
- Continues if shop can sell

---

## 💳 Shop Billing System

### Configuration: `shopBillingPolicy.js`

```javascript
- Commission Rate: 5%
- Fee Start Date: 13/4/2026 18:02:24
- Applies to: All orders with paidAt >= feeStartDate
```

### Flow

1. Order is created → platformFee.status = `pending`
2. Payment confirmed → `updatePlatformFeeOnPayment()` sets status to `unpaid` (awaiting collection)
3. Shop wallet has balance → `syncShopBilling()` auto-charges and sets status to `paid`
4. Shop wallet low → Status remains `unpaid`, shop gets frozen
5. Shop top-up wallet → `syncShopBilling()` retries auto-charge

### Service: `shopBillingService.js`

#### `syncShopBilling(shopId, options)`

- Calculates outstanding platform fees
- Auto-charges from wallet if sufficient balance
- Freezes shop if unpaid fees exceed balance
- Creates notifications
- Returns: `{ shop, summary }`

#### `topUpShopWallet(shopId, amount)`

- Adds balance to shop wallet
- Triggers auto-charge of due fees
- Creates notification

#### `getFrozenShopIds()`

- Returns array of frozen shop IDs

---

## 🔄 API Endpoints

### Authentication Routes (`/api/auth`)

```
POST   /register                    # Register user with password
POST   /register-shop               # Register shop
POST   /send-google-registration-code    # Send OTP for Google signup
POST   /verify-google-registration-code  # Verify OTP and create account
POST   /login                       # Login with email/password
POST   /google-login                # Login with Google ID token
POST   /firebase-sync               # Sync Firebase user
POST   /forgot-password             # Send password reset email
POST   /reset-password              # Reset password with token
GET    /me                          # Get current user profile
PUT    /me                          # Update profile
POST   /set-password                # Set password for OAuth users
PUT    /change-password             # Change password
```

### Product Routes (`/api/products`)

```
GET    /                            # List products (with filters)
GET    /categories                  # Get all categories
GET    /brands                      # Get all brands
GET    /:id                         # Get product details
POST   /                            # Create product (auth required)
PUT    /:id                         # Update product
DELETE /:id                         # Delete product
PATCH  /:id/stock                   # Update stock
POST   /bulk-delete                 # Delete multiple products
```

### Cart Routes (`/api/cart`) - Auth Required

```
GET    /                            # Get cart
POST   /                            # Add to cart
PUT    /:itemId                     # Update item quantity
DELETE /:itemId                     # Remove from cart
POST   /clear                       # Clear cart
POST   /apply-voucher               # Apply voucher code
```

### Order Routes (`/api/orders`) - Auth Required

```
POST   /                            # Create order
GET    /                            # List user's orders
GET    /:id                         # Get order details
POST   /:id/process-payment         # Process payment
PATCH  /:id/status                  # Update status (admin)
PATCH  /:id/mark-paid               # Mark as paid
PATCH  /:id/cancel                  # Cancel order
```

### Shop Routes (`/api/shop`) - Shop Auth Required

```
GET    /products                    # List shop products
POST   /products                    # Create product
PUT    /products/:id                # Update product
DELETE /products/:id                # Delete product
GET    /billing-policy              # Get billing policy
POST   /billing-policy/accept       # Accept billing policy
GET    /billing-summary             # Get shop billing status
POST   /wallet/top-up               # Top-up wallet
POST   /billing/settle              # Settle unpaid fees
GET    /orders                      # List shop's orders
GET    /orders/:id                  # Get order details
PUT    /orders/:id/status           # Update order status
GET    /reviews                     # List reviews
PUT    /reviews/:id/reply           # Reply to review
GET    /notifications               # List notifications
PUT    /notifications/:id/read      # Mark as read
PUT    /notifications/read-all      # Mark all as read
GET    /revenue                     # Revenue analytics
```

### Admin Routes (`/api/admin`) - Admin Auth Required

```
GET    /users                       # List users
GET    /users/count                 # User count
PUT    /users/:id/role              # Change user role
DELETE /users/:id                   # Delete user

GET    /shops                       # List shops
GET    /shops/count                 # Shop count
PUT    /shops/:id/status            # Change shop status

GET    /products                    # List products
GET    /products/count              # Product count

GET    /orders                      # List all orders
GET    /orders/count                # Order count
PUT    /orders/:id/status           # Update status

GET    /reviews                     # List reviews
DELETE /reviews/:id                 # Delete review
PUT    /reviews/:id/reply           # Reply to review

GET    /vouchers                    # List vouchers
POST   /vouchers                    # Create voucher
PUT    /vouchers/:id                # Update voucher
DELETE /vouchers/:id                # Delete voucher

GET    /revenue                     # Platform revenue
GET    /revenue/shops               # Revenue by shop
GET    /revenue/platform            # Platform commission
```

### Other Routes

- `/api/reviews` - Product reviews
- `/api/favorites` - Favorite products
- `/api/feedback` - User feedback
- `/api/posts` - Blog posts
- `/api/vouchers` - Public voucher listing (not auth required)
- `/api/promotions` - Promotions
- `/api/notifications` - Notifications
- `/api/user/addresses` - User addresses
- `/api/locations` - Vietnam provinces/districts

---

## 🎯 Key Features

### 1. User Authentication

- Email/password registration & login
- Google OAuth 2.0 integration
- Firebase authentication support
- OTP verification for registration
- Password reset via email
- Profile management

### 2. E-Commerce Core

- Product catalog with search & filters
- Shopping cart with quantity management
- Order creation with validation
- Multiple payment methods (COD, Stripe, PayPal, Momo, ZaloPay, VNPay)
- Order status tracking with history
- Shipping address management

### 3. Voucher & Promotion System

- Public voucher listing
- Voucher validation (date, min order, usage limit)
- Percentage & fixed discounts
- Automatic discount calculation
- Usage tracking

### 4. Review System

- Product reviews with 1-5 star ratings
- Comment with images
- Shop reply capability
- Helpful marking
- Verified purchase flag
- Automatic product rating update

### 5. Shop Management

- Shop product management
- Order management dashboard
- Shop billing system
- Wallet top-up functionality
- Revenue analytics
- Shop status (active/frozen)
- Billing policy acceptance

### 6. Admin Panel

- User management (view, role assignment, deletion)
- Shop management (status control)
- Product management
- Order management with status updates
- Review moderation
- Voucher & promotion management
- Revenue analytics (platform-wide & by shop)

### 7. Platform Billing

- Automatic 5% commission collection
- Wallet-based settlement
- Shop freezing on unpaid fees
- Auto-unfreeze when fees are paid
- Transaction notifications
- Outstanding debt tracking

### 8. Notification System

- Order status notifications
- Shop order alerts
- Promotion & voucher announcements
- Review notifications
- Billing notifications
- Expiring voucher reminders
- System notifications

### 9. Data & Content

- Homepage banners
- Blog posts
- Static location data (Vietnam provinces)
- FAQ/Help content

---

## 🔧 Configuration

### Environment Variables

```
# Database
MONGO_URI=mongodb://localhost:27017/md02_datn

# JWT
JWT_SECRET=secret_jwt_key
JWT_EXPIRE=7d

# Email (Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=465
MAIL_SECURE=true
MAIL_USER=your_email@gmail.com
MAIL_PASS=your_app_password

# OAuth
GOOGLE_CLIENT_ID=your_google_client_id

# Firebase
FIREBASE_PROJECT_ID=your_firebase_project

# Auto seeding
AUTO_SEED_ON_START=true

# Debug
DEBUG_AUTH=false

# Role inference
ADMIN_EMAILS=admin@test.com
SHOP_EMAILS=shop@test.com
ADMIN_EMAIL_DOMAINS=company.com
```

---

## 📊 Data Models Relationships

```
User (1) ──── (Many) Product (created/owned)
User (1) ──── (Many) Order
User (1) ──── (Many) Cart (unique)
User (1) ──── (Many) Review
User (1) ──── (Many) Favorite
User (1) ──── (Many) Notification

Product (1) ──── (Many) Order Item
Product (1) ──── (Many) Review
Product (1) ──── (Many) Promotion

Order (1) ──── (Many) Order Item
Order (Many) ──── (One) User

Shop (User) (1) ──── (Many) Product
Shop (User) (1) ──── (Many) Order Item
```

---

## 🚀 Startup Flow

1. Load environment variables
2. Connect to MongoDB
3. Initialize Firebase Admin SDK
4. Initialize email transporter
5. Setup Express middleware
6. Register routes
7. Setup error handler
8. Start server on PORT (default 5000)
9. Auto-seed database if enabled

---

## 🔔 Job Scheduling

### voucherExpiryNotifier

- Runs daily at 2:00 AM
- Finds vouchers expiring within 3 days
- Creates notifications for users
- Prevents duplicate notifications

---

## 📝 Notes

1. **Shop Freeze Logic**: Shops are automatically frozen when platform fees go unpaid and wallet balance is insufficient
2. **Commission Calculation**: 5% applies only to orders paid after 13/4/2026 18:02:24
3. **Platform Fee Auto-Charge**: When shop top-ups wallet or new income arrives, system auto-charges due fees
4. **Frozen Shop Visibility**: Products from frozen shops don't appear in public listings
5. **Email**: Only Gmail SMTP is supported (App Password required, not account password)
6. **Firebase**: Firebase tokens are accepted for authentication alongside custom JWT

---

## 📦 Dependencies

**Core**: express, mongoose, dotenv, cors, helmet, morgan
**Auth**: jsonwebtoken, bcryptjs, passport, firebase-admin, google-auth-library
**Payment**: stripe, paypal-rest-sdk
**Utils**: axios, uuid, multer, express-validator, express-rate-limit
**Email**: nodemailer
**Real-time**: socket.io
**Cloud**: cloudinary
**Task Scheduling**: node-cron
**Testing**: jest, supertest, nodemon

---

## 🎓 Summary

This is a full-featured **e-commerce platform backend** with:

- Multi-user support (user, shop, admin)
- Complete product & order management
- Integrated payment gateway support
- Real-time notifications
- Advanced billing system with auto-settlement
- Review & feedback system
- Promotion & discount management
- Comprehensive admin dashboard

The platform is designed for scalability and includes features like wallet management, automatic fee collection, and shop status management based on payment compliance.
