# Android App - English Text Strings Summary

## Overview
This document contains all English text strings found across the Android app, organized by category.

---

## 1. Main UI Texts (Menus, Buttons, Labels)

### Navigation & Headers
- **"Shopping Cart"** - [activity_cart.xml](activity_cart.xml)
- **"Checkout"** - [activity_check_out.xml](activity_check_out.xml)
- **"Edit Profile"** - [activity_edit_profile.xml](activity_edit_profile.xml)
- **"ELECTRONICS AND INNOVATION"** - [activity_loading.xml](activity_loading.xml)
- **"INITIALIZING SYSTEMS"** - [activity_loading.xml](activity_loading.xml)

### Login & Authentication
- **"CORE-TECH"** - Multiple files (activity_login.xml, activity_loading.xml, activity_reset_pass.xml)
- **"Welcome to CORE- TECH"** - [activity_login.xml](activity_login.xml)
- **"Your gateway to the next generation of electronics."** - [activity_login.xml](activity_login.xml)
- **"Create Account"** - [activity_register.xml](activity_register.xml)
- **"Join Core-Tech for the best electronics deals."** - [activity_register.xml](activity_register.xml)
- **"Reset Password"** - [activity_reset_pass.xml](activity_reset_pass.xml)
- **"Enter the email or phone number associated with your account and we'll send an OTP to verify your identity."** - [activity_reset_pass.xml](activity_reset_pass.xml)
- **"Verify Your Identity"** - [activity_otp.xml](activity_otp.xml)
- **"Enter the 6-digit code sent to your email"** - [activity_otp.xml](activity_otp.xml)

### Product & Cart Details
- **"Product Name"** - [bottom_sheet_product_options.xml](bottom_sheet_product_options.xml)
- **"Stock: 0"** - [bottom_sheet_product_options.xml](bottom_sheet_product_options.xml)
- **"Color"** - [bottom_sheet_product_options.xml](bottom_sheet_product_options.xml)
- **"Size"** - [bottom_sheet_product_options.xml](bottom_sheet_product_options.xml)
- **"Quantity"** - [bottom_sheet_product_options.xml](bottom_sheet_product_options.xml)
- **"+ ADD TO CART"** - [bottom_sheet_product_options.xml](bottom_sheet_product_options.xml)
- **"You also like"** - [activity_detail.xml](activity_detail.xml)

### Buttons & Actions
- **"Login"** - [activity_login.xml](activity_login.xml)
- **"Register"** - [activity_register.xml](activity_register.xml)
- **"Sign Up"** - [activity_login.xml](activity_login.xml)
- **"Send OTP >"** - [activity_reset_pass.xml](activity_reset_pass.xml)
- **"Back to Login"** - [activity_reset_pass.xml](activity_reset_pass.xml)
- **"Verify"** - [activity_otp.xml](activity_otp.xml)
- **"Resend OTP"** - [activity_otp.xml](activity_otp.xml)
- **"Add to Cart"** - [activity_detail.xml](activity_detail.xml)
- **"Save Changes"** - [activity_edit_profile.xml](activity_edit_profile.xml)
- **"Cancel"** - [activity_edit_profile.xml](activity_edit_profile.xml)

### Checkout & Payment
- **"Shipping Address"** - [activity_check_out.xml](activity_check_out.xml)
- **"Payment Method"** - [activity_check_out.xml](activity_check_out.xml)
- **"Your Items"** - [activity_check_out.xml](activity_check_out.xml)
- **"Voucher"** - [activity_check_out.xml](activity_check_out.xml)
- **"All"** - [activity_cart.xml](activity_cart.xml)

### Profile & Account
- **"ACCOUNT"** - [fragment_profile.xml](fragment_profile.xml)
- **"My Orders"** - [fragment_profile.xml](fragment_profile.xml)
- **"Shipping Address"** - [fragment_profile.xml](fragment_profile.xml)
- **"Payment Methods"** - [fragment_profile.xml](fragment_profile.xml)
- **"Wishlist"** - [fragment_profile.xml](fragment_profile.xml)
- **"SETTINGS"** - [fragment_profile.xml](fragment_profile.xml)
- **"Notifications"** - [fragment_profile.xml](fragment_profile.xml)
- **"Privacy & Security"** - [fragment_profile.xml](fragment_profile.xml)
- **"Language"** - [fragment_profile.xml](fragment_profile.xml)
- **"English"** - [fragment_profile.xml](fragment_profile.xml)
- **"Dark Mode"** - [fragment_profile.xml](fragment_profile.xml)
- **"Logout"** - [fragment_profile.xml](fragment_profile.xml)

### Financial Information
- **"Subtotal: $0"** - [activity_cart.xml](activity_cart.xml)
- **"Tax: $0"** - [activity_cart.xml](activity_cart.xml)
- **"Order Total: $0"** - [activity_cart.xml](activity_cart.xml)
- **"Subtotal: $0.00"** - [activity_check_out.xml](activity_check_out.xml)
- **"Tax: $0.00"** - [activity_check_out.xml](activity_check_out.xml)
- **"Total: $1249"** - [activity_check_out.xml](activity_check_out.xml)

### Social Login
- **"OR CONTINUE WITH"** - [activity_login.xml](activity_login.xml)
- **"Google"** - [activity_login.xml](activity_login.xml)
- **"FaceBook"** - [activity_login.xml](activity_login.xml)

---

## 2. Toast Messages

### Authentication & Login
- **"Nhập đầy đủ"** (Vietnamese - "Enter fully") - LoginActivity, RegisterActivity
- **"Email không hợp lệ"** (Vietnamese - "Invalid email") - RegisterActivity
- **"Mật khẩu không khớp"** (Vietnamese - "Password mismatch") - RegisterActivity
- **"Firebase lỗi: {error}"** (Vietnamese - "Firebase error") - RegisterActivity
- **"Đăng ký thành công"** (Vietnamese - "Registration successful") - RegisterActivity
- **"Email đã tồn tại hoặc lỗi server"** (Vietnamese - "Email exists or server error") - RegisterActivity
- **"Login thành công"** (Vietnamese - "Login successful") - LoginActivity
- **"Sai tài khoản hoặc mật khẩu"** (Vietnamese - "Wrong account or password") - LoginActivity
- **"Không lấy được token"** (Vietnamese - "Cannot get token") - LoginActivity
- **"Google login fail"** - LoginActivity
- **"Firebase fail"** - LoginActivity
- **"Google account không có email"** (Vietnamese - "Google account has no email") - LoginActivity
- **"Google login thành công"** (Vietnamese - "Google login successful") - LoginActivity
- **"Sync server lỗi: {error}"** (Vietnamese - "Sync server error") - LoginActivity
- **"Lỗi tạo request sync server"** (Vietnamese - "Error creating sync server request") - LoginActivity

### Product Operations
- **"Stock: 0"** - BottomSheetProductOptions
- **"You have {count} notification(s)"** - HomeFragment
- **"No new notifications"** - HomeFragment
- **"Invalid product data"** - DetailActivity
- **"Invalid product"** - DetailActivity

### Cart & Checkout
- **"Please login to checkout"** - CheckOutActivity, CartActivity
- **"Vui lòng chọn phương thức thanh toán"** (Vietnamese - "Please select payment method") - CheckOutActivity
- **"Thanh toán thành công!"** (Vietnamese - "Payment successful!") - CheckOutActivity
- **"Vui lòng điền đủ thông tin"** (Vietnamese - "Please fill in all information") - ShippingAddressActivity

### Profile & Settings
- **"My Orders"** - ProfileFragment
- **"Payment Methods"** - ProfileFragment
- **"Wishlist"** - ProfileFragment
- **"Notifications"** - ProfileFragment
- **"Privacy & Security"** - ProfileFragment
- **"Change language"** - ProfileFragment
- **"Dark mode on"** - ProfileFragment
- **"Dark mode off"** - ProfileFragment
- **"Vui lòng điền tên và email"** (Vietnamese - "Please enter name and email") - EditProfileActivity
- **"Lỗi lưu vào database"** (Vietnamese - "Database save error") - EditProfileActivity
- **"Cập nhật thành công"** (Vietnamese - "Update successful") - EditProfileActivity
- **"Cập nhật Firebase không thành công"** (Vietnamese - "Firebase update failed") - EditProfileActivity

### Order Operations
- **"Không thể hủy đơn hàng ở trạng thái hiện tại"** (Vietnamese - "Cannot cancel order in current status") - OrderDetailActivity
- **"Vui lòng chọn lý do hủy"** (Vietnamese - "Please select cancellation reason") - OrderDetailActivity
- **"Đơn hàng #{id} đã bị hủy\nLý do: {reason}"** (Vietnamese - "Order #{id} cancelled\nReason: {reason}") - OrderDetailActivity
- **"Lỗi khi hủy đơn hàng"** (Vietnamese - "Error cancelling order") - OrderDetailActivity
- **"Mở chat với shop"** (Vietnamese - "Open chat with shop") - OrderDetailActivity
- **"Mua lại đơn hàng"** (Vietnamese - "Repurchase order") - OrderDetailActivity
- **"Gọi shop"** (Vietnamese - "Call shop") - OrderDetailActivity
- **"Mở trung tâm hỗ trợ"** (Vietnamese - "Open support center") - OrderDetailActivity
- **"Yêu cầu hoàn tiền"** (Vietnamese - "Request refund") - OrderDetailActivity
- **"Vui lòng chọn số sao"** (Vietnamese - "Please select rating") - OrderDetailActivity
- **"Vui lòng nhập nhận xét"** (Vietnamese - "Please enter comment") - OrderDetailActivity
- **"Cảm ơn đánh giá của bạn ({rating} sao)"** (Vietnamese - "Thank you for your rating ({rating} stars)") - OrderDetailActivity
- **"Lỗi khi mở chi tiết đơn hàng: {message}"** (Vietnamese - "Error opening order details: {message}") - OrderDetailActivity

---

## 3. Product Descriptions

### Database Sample Data (from DBHelper.java)

#### Laptops
1. **"MacBook Pro M3"** - "Powerful laptop with Apple M3 chip, 16GB RAM and stunning Retina display. Perfect for developers and creators."
2. **"Dell XPS 15"** - "High-end Windows laptop with powerful performance, premium design and stunning display."

#### Smartphones
1. **"iPhone 15"** - "Latest Apple smartphone with A17 chip, dynamic island, improved cameras and all-day battery life."
2. **"Samsung Galaxy S25"** - "Flagship Samsung phone with powerful processor, advanced camera system and beautiful AMOLED display."
3. **"Google Pixel 8 Pro"** - "Google flagship phone with AI camera features and pure Android experience."

#### Headphones & Audio
1. **"Sony WH-1000XM5"** - "Industry leading noise cancelling headphones with crystal clear sound and 30 hours battery life."
2. **"AirPods Pro 2"** - "Premium Apple wireless earbuds with active noise cancellation and spatial audio."
3. **"JBL Tune 760NC"** - "Wireless headphones with active noise cancelling and deep bass sound."

#### Accessories
1. **"Cáp sạc iPhone"** (iPhone Charging Cable) - "Cáp Lightning chuẩn Apple, bền bỉ và an toàn cho mọi thiết bị." (Standard Apple Lightning cable, durable and safe for all devices.)
2. **"Ốp lưng Samsung"** (Samsung Case) - "Ốp lưng chống sốc cao cấp, bảo vệ điện thoại khỏi va đập." (Premium shock-resistant case protects phone from impact.)
3. **"Pin dự phòng 20000mAh"** (20000mAh Power Bank) - "Pin dự phòng dung lượng lớn, sạc nhanh và an toàn." (Large capacity power bank, fast charge and safe.)

#### Smartwatches
1. **"Apple Watch Series 9"** - "Đồng hồ thông minh Apple mới nhất với thiết kế thanh lịch và tính năng sức khỏe toàn diện." (Latest Apple smartwatch with elegant design and comprehensive health features.)
2. **"Samsung Galaxy Watch 6"** - "Đồng hồ thông minh Samsung với màn hình AMOLED sắc nét và pin lâu dài." (Samsung smartwatch with crisp AMOLED display and long battery life.)
3. **"Fitbit Charge 6"** - "Vòng theo dõi sức khỏe Fitbit với cảm biến độc lập và ứng dụng thông minh." (Fitbit health tracker with independent sensors and smart app.)

### Product Categories
- **"Laptop"**
- **"Điện thoại"** (Smartphone)
- **"Tai nghe"** (Headphones)
- **"Phụ kiện"** (Accessories)
- **"Đồng hồ"** (Watches)

---

## 4. UI Hints and Placeholders

### Login & Authentication
- **"Enter your email"** - [activity_login.xml](activity_login.xml)
- **"Enter your password"** - [activity_login.xml](activity_login.xml)
- **"Password"** - [activity_login.xml](activity_login.xml)

### Registration
- **"Enter your full name"** - [activity_register.xml](activity_register.xml)
- **"example@coretech.com"** - [activity_register.xml](activity_register.xml)
- **"+1 (555) 000-0000"** - [activity_register.xml](activity_register.xml)
- **"Create a strong password"** - [activity_register.xml](activity_register.xml)
- **"Password"** - [activity_register.xml](activity_register.xml)

### Password Reset
- **"Enter email or phone"** - [activity_reset_pass.xml](activity_reset_pass.xml)
- **"Format: name@domain.com or +1 234 567 8900"** - [activity_reset_pass.xml](activity_reset_pass.xml)

### Profile Settings
- **"Alex Johnson"** - [activity_edit_profile.xml](activity_edit_profile.xml)
- **"alex.johnson@coretech.io"** - [activity_edit_profile.xml](activity_edit_profile.xml)
- **"+1 555-0123-4567"** - [activity_edit_profile.xml](activity_edit_profile.xml)
- **"Senior Hardware Architect..."** - [activity_edit_profile.xml](activity_edit_profile.xml)

### Checkout & Shopping
- **"Enter voucher code"** - [activity_check_out.xml](activity_check_out.xml)
- **"Search tech, brands..."** - [fragment_home.xml](fragment_home.xml), [fragment_browse.xml](fragment_browse.xml)

### Order Details
- **"Nhập nhận xét của bạn..."** (Vietnamese - "Enter your comment...") - [activity_order_detail.xml](activity_order_detail.xml)

### Additional Placeholders
- **"Decrease Quantity"** - [bottom_sheet_product_options.xml](bottom_sheet_product_options.xml)
- **"Increase Quantity"** - [bottom_sheet_product_options.xml](bottom_sheet_product_options.xml)
- **"Back"** - [activity_detail.xml](activity_detail.xml)
- **"Edit"** - [item_address.xml](item_address.xml)
- **"Delete"** - [item_address.xml](item_address.xml)
- **"Banner image"** - [item_banner.xml](item_banner.xml)

---

## 5. Activity/Fragment Titles and Navigation Labels

### Main Titles
- **"Core-Tech"** - Home header [fragment_home.xml](fragment_home.xml)
- **"Categories"** - [fragment_home.xml](fragment_home.xml)

### Account Sections
- **"ORDERS"** - [fragment_profile.xml](fragment_profile.xml) (Count: 24)
- **"WISHLIST"** - [fragment_profile.xml](fragment_profile.xml) (Count: 12)

### Order Management
- **"Lịch sử đơn hàng"** (Vietnamese - "Order History") - [fragment_orders.xml](fragment_orders.xml)
- **"Tất cả"** (Vietnamese - "All") - [fragment_orders.xml](fragment_orders.xml)
- **"Chưa thanh toán"** (Vietnamese - "Unpaid") - [fragment_orders.xml](fragment_orders.xml)
- **"Đang xử lý"** (Vietnamese - "Processing") - [fragment_orders.xml](fragment_orders.xml)
- **"Đang giao hàng"** (Vietnamese - "Shipping") - [fragment_orders.xml](fragment_orders.xml)
- **"Đã nhận"** (Vietnamese - "Received") - [fragment_orders.xml](fragment_orders.xml)
- **"Đã hủy"** (Vietnamese - "Cancelled") - [fragment_orders.xml](fragment_orders.xml)
- **"Chi tiết"** (Vietnamese - "Details") - [item_order.xml](item_order.xml)

### Contact & Support
- **"Liên hệ cửa hàng"** (Vietnamese - "Contact Store") - [activity_order_detail.xml](activity_order_detail.xml)
- **"💬 Nhắn tin với shop"** (Vietnamese - "💬 Message Shop") - [activity_order_detail.xml](activity_order_detail.xml)
- **"☎ Gọi cửa hàng"** (Vietnamese - "☎ Call Store") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Hỗ trợ"** (Vietnamese - "Support") - [activity_order_detail.xml](activity_order_detail.xml)
- **"🏢 Trung tâm hỗ trợ"** (Vietnamese - "🏢 Support Center") - [activity_order_detail.xml](activity_order_detail.xml)
- **"💰 Yêu cầu hoàn tiền"** (Vietnamese - "💰 Request Refund") - [activity_order_detail.xml](activity_order_detail.xml)

### Page Sections
- **"Full Name"** - [activity_edit_profile.xml](activity_edit_profile.xml)
- **"Email Address"** - [activity_edit_profile.xml](activity_edit_profile.xml)
- **"Phone Number"** - [activity_edit_profile.xml](activity_edit_profile.xml)
- **"Professional Bio"** - [activity_edit_profile.xml](activity_edit_profile.xml)
- **"I agree to the Terms of Service and Privacy Policy."** - [activity_register.xml](activity_register.xml)
- **"Remembered your password?"** - [activity_reset_pass.xml](activity_reset_pass.xml)
- **"Didn't receive code?"** - [activity_otp.xml](activity_otp.xml)
- **"Don't have an account?"** - [activity_login.xml](activity_login.xml)

### Order-Related Labels
- **"Danh sách hàng hóa"** (Vietnamese - "Product List") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Trạng thái"** (Vietnamese - "Status") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Địa chỉ giao hàng"** (Vietnamese - "Shipping Address") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Số lượng sản phẩm:"** (Vietnamese - "Product Quantity:") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Dự kiến nhận:"** (Vietnamese - "Expected Delivery:") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Tổng tiền:"** (Vietnamese - "Total Amount:") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Đánh giá sản phẩm"** (Vietnamese - "Rate Product") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Gửi đánh giá"** (Vietnamese - "Send Rating") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Chi tiết đơn hàng"** (Vietnamese - "Order Details") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Mã đơn hàng:"** (Vietnamese - "Order Code:") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Phương thức thanh toán:"** (Vietnamese - "Payment Method:") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Thời gian đặt:"** (Vietnamese - "Order Time:") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Thời gian nhận:"** (Vietnamese - "Delivery Time:") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Hủy đơn"** (Vietnamese - "Cancel Order") - [activity_order_detail.xml](activity_order_detail.xml)
- **"Mua lại"** (Vietnamese - "Repurchase") - [activity_order_detail.xml](activity_order_detail.xml)

### Loading Screen
- **"0%"** - [activity_loading.xml](activity_loading.xml)

---

## 6. Sample User Data (Placeholders)

### Default User Profile
- **Name:** "Alex Johnson"
- **Email:** "alex.johnson@coretech.io"
- **Phone:** "+1 (555) 012-3456"
- **Address:** "123 Tech Lane, Silicon District"
- **Orders:** 24
- **Wishlist:** 12

---

## Summary Statistics

| Category | Count |
|----------|-------|
| Main UI Texts | 70+ |
| Toast Messages | 50+ |
| Product Descriptions | 12 |
| UI Hints/Placeholders | 30+ |
| Activity/Fragment Titles | 40+ |
| **Total English Text Items** | **200+** |

---

## Notes

- **Mixed Language**: The app uses both English and Vietnamese (primarily Vietnamese for user-facing content)
- **Location**: Most English text is in:
  - Activity XML layouts (login, register, checkout, profile)
  - Product information (from database)
  - Placeholder texts for user input
  - Some Toast notifications
- **Key Files by English Content**:
  - `activity_login.xml` - Authentication UI
  - `activity_register.xml` - Registration form
  - `activity_edit_profile.xml` - Profile management
  - `activity_check_out.xml` - Checkout flow
  - `DBHelper.java` - Product descriptions
  - Multiple Activity classes - Toast notifications

