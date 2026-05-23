# Backend Controllers Detailed Summary

## 📋 Overview

14 controller files implementing business logic for the e-commerce platform.

---

## 🔐 authController.js

### Key Functions

#### 1. **sendGoogleRegistrationCode()**

- Validates Google ID token
- Checks if email already registered
- Generates OTP code (100000-999999)
- Stores OTP in memory with 10-minute TTL
- Sends OTP via email

#### 2. **verifyGoogleRegistrationCode()**

- Verifies OTP code
- Creates new user with Google data
- Infers role based on email domain/list
- Generates JWT token
- Cleans up OTP store

#### 3. **register()**

- Email validation & duplicate check
- Bcrypt password hashing (10 rounds)
- Creates user with role='user'
- Generates JWT token
- Returns user & token

#### 4. **login()**

- Email/password validation
- Bcrypt password comparison
- Checks if account is Google-only (no password)
- Generates JWT token
- Returns user & token

#### 5. **registerShop()**

- Similar to register but role='shop'
- Used for shop-specific registration

#### 6. **firebaseSync()**

- Syncs Firebase user with MongoDB
- Creates user if doesn't exist
- Infers role based on email
- Generates temporary password if needed
- Returns JWT token & temporary password

#### 7. **googleLogin()**

- Verifies Google ID token
- Finds existing user by email
- Updates firebaseUid if new
- Infers role based on email
- Returns JWT token

#### 8. **verifyGoogleToken()**

- First tries Firebase Admin verification
- Falls back to OAuth2Client verification
- Handles audience mismatches
- Returns: uid, email, name, verified flag

#### 9. **resetPassword()**

- Validates reset token expiry
- Bcrypt hashes new password
- Clears reset token
- Updates user

#### 10. **forgotPassword()**

- Generates reset token (crypto)
- Sets 24-hour expiry
- Sends reset email with token link

#### 11. **me()**

- Returns current user profile (excluding password)
- Requires auth

#### 12. **updateProfile()**

- Updates user name, phone, avatar
- Returns updated user

#### 13. **setPassword()**

- For users who logged in via OAuth
- Sets password for future email/password login

#### 14. **changePassword()**

- For logged-in users
- Validates old password
- Hashes new password
- Updates user

### Role Inference Logic

```
ADMIN_EMAILS → admin role
SHOP_EMAILS → shop role
ADMIN_EMAIL_DOMAINS → admin role
Otherwise → user role
```

---

## 📦 productController.js

### Shop Functions

#### **getShopProducts()**

- Filters products by shopId
- Supports search, category filter
- Pagination & sorting

#### **getMyProducts()**

- Gets products for current shop user
- Same as getShopProducts but req.user.id

#### **createMyProduct()**

- Creates product for shop
- Sets shopId = req.user.id
- Returns 201 with new product

#### **updateMyProduct()**

- Updates only shop's own products
- Validates shopId ownership

#### **deleteMyProduct()**

- Deletes only shop's own products

### Admin Functions

#### **getProducts()**

- Lists all products with pagination
- Includes product details & shop info

#### **createProduct()**

- Admin can create products for any shop
- Sets createdBy = req.user.id
- Can specify shopId in body

#### **updateProduct()**

- Admin can update any product

#### **deleteProduct()**

- Admin can delete any product

### Public Functions

#### **getProducts()**

- Lists all products with filters
- Filters: q (search), category, brand, minPrice, maxPrice, minRating, featured, hot
- **Excludes products from frozen shops**
- Pagination (default limit=10000)
- Sorting (default by createdAt DESC)

#### **getProductById()**

- Returns product details
- Checks if shop is frozen → returns 404

#### **getCategories()**

- Distinct categories

#### **getBrands()**

- Distinct brands

#### **updateStock()**

- Updates product stock

#### **bulkDelete()**

- Deletes multiple products by IDs

### Revenue Calculation

#### **getMyRevenue()**

- Period: day, week, month, year
- Aggregates delivered orders only
- Groups by period
- Calculates totalRevenue, totalOrders, avgOrderValue, itemCount

---

## 📋 orderController.js

### Order Creation & Management

#### **createOrder()**

- Validates items array & shipping address
- Validates address fields (name, phone, city, district, ward)
- Phone validation: `0\d{9}` pattern
- City name validation: no numbers
- Checks all products exist & are active
- **Checks if any shop is frozen** → returns 400
- Maps products to shops (multi-shop order support)
- Creates order with initial status='chờ xác nhận'
- Platform fee: initial status='pending'
- Creates notifications for each affected shop
- Returns 201 with order

#### **getOrders()**

- Admin: returns all orders
- User: returns only their orders
- Populates product details

#### **getOrderById()**

- Validates user ownership or admin
- Returns 404 if not owned

#### **updateStatus()**

- Admin only
- Updates order.status
- Updates statusHistory with timestamp

#### **processPayment()**

- Marks payment.status = 'paid'
- Sets paidAt timestamp
- Calls updatePlatformFeeOnPayment()
- Calls settlePaymentAndCreditShops()

#### **markPaid()**

- Alternative to processPayment
- Manual payment marking

#### **cancelOrder()**

- User can cancel if not yet shipped/delivered
- Admin can cancel any order

### Platform Fee Functions

#### **updatePlatformFeeOnPayment()**

- Called when order is paid
- Checks if order.payment.paidAt >= feeStartDate
- If yes: calculates 5% fee, sets status='unpaid'
- If no: sets eligible=false (no fee for pre-policy orders)
- Updates each item's platformFee object
- Saves order

#### **settlePaymentAndCreditShops()**

- Called when payment confirmed
- Aggregates gross & fee per shop
- Credits shop wallet with (gross - fee)
- Marks platformFee.status='paid'
- Creates notification for each shop with amount & fee details
- Returns updated order

---

## 🛒 cartController.js

### Cart Operations

#### **getCart()**

- Finds cart by user
- Creates empty cart if not found
- Populates product details

#### **addToCart()**

- Validates product exists & is active
- Checks stock sufficiency
- If item with same product & variant exists → increment qty
- Otherwise → add new item
- Returns updated cart

#### **updateCartItem()**

- Validates qty >= 1
- Checks stock for new qty
- Updates item qty
- Returns updated cart

#### **removeFromCart()**

- Removes item by itemId
- Returns updated cart

#### **clearCart()**

- Empties items array
- Clears voucher
- Returns empty cart

#### **applyVoucher()**

- Finds active voucher by code
- Checks if not expired
- Checks usage limit not exceeded
- Validates minimum order value
- Calculates discount based on type:
  - Percentage: (subtotal × value) / 100
  - Fixed: value amount
- Applies max discount cap
- Sets cart.voucher with code, discount, type
- Returns updated cart

#### **removeVoucher()**

- Clears voucher
- Returns updated cart

#### **getCartSummary()**

- Calculates subtotal from items
- Applies voucher discount
- Estimates shipping: 30000đ standard, free if >500k
- Returns summary object

---

## ⭐ reviewController.js

### Review Management

#### **createReview()**

- Unique constraint: one review per user per product
- User can only review products they bought
- Updates product.ratings automatically
- Creates shop notification
- Returns 201 with review

#### **updateProductRatings()**

- Calculates average rating
- Counts total reviews
- Updates product with new ratings
- Called after review create/update/delete

#### **updateReview()**

- User can only update their own review (or admin)
- Updates rating & comment
- Recalculates product ratings
- Returns updated review

#### **deleteReview()**

- User can only delete their own review (or admin)
- Recalculates product ratings
- Returns 200

#### **listByProduct()**

- Lists all reviews for a product
- Populates user & shop response info
- Returns array

---

## 🎟️ voucherController.js

#### **getActiveVouchers()**

- Public endpoint (no auth required)
- Filters: isActive=true, startDate <= now, endDate >= now
- Excludes admin fields (createdBy, limits, counts)
- Returns active vouchers

---

## 🎁 promotionController.js

### Promotion Management

#### **getPromotions()**

- Lists promotions with filter by isActive
- Pagination support
- Sorts by createdAt DESC

#### **getPromotionById()**

- Returns promotion details

#### **createPromotion()**

- Only admin/authenticated users
- Sets createdBy = req.user.id
- Unique code constraint

#### **updatePromotion()**

- Updates promotion details
- Unique code validation

#### **deletePromotion()**

- Deletes promotion

#### **applyDiscountCode()**

- Validates code exists & is active
- Checks min order value
- Iterates through cart items
- Calculates applicable discount:
  - Percentage: (product.price × promotion.value / 100) × item.quantity
  - Fixed: min(value, product.price × item.quantity)
  - Applies maxDiscount cap
- Returns discount breakdown by item

---

## 🏪 shopController.js

### Shop Operations

#### **getShopProducts()**

- Lists products for current shop
- Returns billing status per product

#### **createProduct()**

- Creates product for shop
- Sets shopId & createdBy
- Returns product with billing status

#### **updateProduct()**

- Updates shop product
- Checks ownership

#### **deleteProduct()**

- Deletes shop product

#### **getBillingSummary()**

- Calls syncShopBilling()
- Returns summary, policy, overdue items
- Fetches unpaid orders with details

#### **topUpWallet()**

- Validates amount > 0
- Adds to wallet.balance
- Updates lastTopUpAt
- Calls syncShopBilling() for auto-charge
- Returns updated summary

#### **settleBilling()**

- Calls syncShopBilling()
- Returns settlement status

#### **getBillingPolicy()**

- Returns current platform fee policy

#### **acceptBillingPolicy()**

- Records acceptance timestamp
- Records policy version
- Returns shop with policy payload

#### **getShopOrders()**

- Lists orders for shop
- Filters by items.shopId

#### **getShopOrderById()**

- Returns order with shop's items

#### **updateShopOrderStatus()**

- Shop updates their own orders

#### **getShopReviews()**

- Reviews for shop's products

#### **replyShopReview()**

- Shop can reply to reviews
- Sets response.text, respondedAt, respondedBy

#### **getShopNotifications()**

- Notifications for shop

#### **markNotificationRead()**

- Sets isRead=true

#### **markAllNotificationsRead()**

- Sets all shop notifications to read

#### **getShopRevenue()**

- Revenue analytics for shop
- Calculates by period

---

## 👨‍💼 adminController.js

### User Management

- **getUsers()** - Lists all users with pagination
- **getUserCount()** - Total user count
- **updateUserRole()** - Change user role
- **deleteUser()** - Delete user

### Shop Management

- **getShops()** - Lists all shops
- **getShopCount()** - Total shop count
- **updateShopStatus()** - Change shop active status

### Product Management

- **getProducts()** - Lists all products with pagination
- **getProductCount()** - Total product count
- **createProduct()** - Admin creates products
- **updateProduct()** - Admin updates any product
- **deleteProduct()** - Admin deletes any product

### Order Management

- **getOrders()** - Lists all orders (supports Vietnamese status mapping)
- **getOrderCount()** - Total order count
- **updateOrderStatus()** - Admin updates order status
- **getOrderStats()** - Statistics by date/month

### Review Management

- **getReviews()** - Lists all reviews
- **getReviewCount()** - Total review count
- **deleteReview()** - Remove review
- **replyReview()** - Admin replies to reviews

### Feedback Management

- **getFeedbacks()** - Lists user feedback
- **deleteFeedback()** - Delete feedback

### Post Management

- **getPosts()** - Lists blog posts
- **createPost()** - Create blog post
- **updatePost()** - Update blog post
- **deletePost()** - Delete blog post

### Voucher Management

- **getVouchers()** - Lists all vouchers
- **createVoucher()** - Create voucher
- **updateVoucher()** - Update voucher
- **deleteVoucher()** - Delete voucher

### Revenue Analytics

- **getRevenue()** - Platform revenue stats
- **getRevenueByShop()** - Revenue breakdown by shop
- **getPlatformRevenue()** - Platform commission earned
- **getPlatformRevenueByShop()** - Commission per shop

---

## 📞 Other Controllers

### favoriteController.js

- **addToFavorite()** - Add product to favorites
- **removeFromFavorite()** - Remove from favorites
- **getUserFavorites()** - List user's favorites
- **isFavorite()** - Check if favorited

### feedbackController.js

- **submitFeedback()** - Submit user feedback
- **getFeedbacks()** - List feedbacks (admin)

### postController.js

- **getPosts()** - List blog posts
- **getPostById()** - Get post details
- **createPost()** - Create (admin)
- **updatePost()** - Update (admin)
- **deletePost()** - Delete (admin)

### addressController.js

- **getAddresses()** - User's saved addresses
- **addAddress()** - Add new address
- **updateAddress()** - Update address
- **deleteAddress()** - Remove address
- **setDefaultAddress()** - Set as default

### notificationController.js

- **getNotifications()** - User's notifications
- **markAsRead()** - Mark single as read
- **markAllAsRead()** - Mark all as read
- **deleteNotification()** - Delete notification

### homeController.js

- **getHomePage()** - Homepage data (banners, featured products, etc.)
- **getFeaturedProducts()** - Featured products
- **getHotDeals()** - Hot deal products
- **getNewArrivals()** - New products

---

## 🔄 Data Flow Examples

### Order Purchase Flow

1. User adds items to cart
2. User applies voucher (optional)
3. User creates order
   - Items validated
   - Shop status checked
   - Shipping address validated
   - platformFee.status = 'pending'
4. User pays (payment gateway)
5. Server receives payment webhook
6. `updatePlatformFeeOnPayment()` calculates fee
7. `settlePaymentAndCreditShops()` credits shops
8. Shops notified of payment received

### Shop Freezing Flow

1. Order payment received
2. Shop wallet insufficient for fee → platformFee.status='unpaid'
3. `syncShopBilling()` checks outstanding amount
4. outstandingAmount > 0 → shop frozen
5. Shop notification: "Account frozen, outstanding: XYZđ"
6. Shop top-ups wallet
7. `syncShopBilling()` auto-charges fee
8. outstandingAmount = 0 → shop unfrozen
9. Shop notification: "Account activated"

### Review & Rating Flow

1. User creates review
2. `updateProductRatings()` calculates new average
3. Product.ratings updated
4. Shop notified
5. User can update/delete review
6. Ratings recalculated after each change

---

## 📊 Key Validations

### Order Creation

- ✅ Items array not empty
- ✅ Shipping address complete
- ✅ Phone number format: `0\d{9}`
- ✅ City name: no numbers
- ✅ Products exist & active
- ✅ Sufficient stock
- ✅ No frozen shops

### Cart Voucher

- ✅ Code exists & active
- ✅ Not expired
- ✅ Usage limit not exceeded
- ✅ Min order value met
- ✅ Cart not empty

### Review

- ✅ One review per user per product
- ✅ Rating 1-5 stars

### Product Stock

- ✅ Stock >= quantity requested

---

## 🎯 Summary

Controllers implement complete business logic for:

- Multi-vendor marketplace
- Automatic billing & commission collection
- Shop freeze/unfreeze on payment status
- Complex cart & order management
- Review system with product rating updates
- Admin dashboard with analytics
- Real-time notifications
- Role-based access control (user, shop, admin)
