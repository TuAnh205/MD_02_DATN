# 🏪 CẬP NHẬT HỆ THỐNG TÍNH PHÍ SÀN CHO SHOP

## 📋 Tóm Tắt Thay Đổi

Đã cập nhật hệ thống quản lý doanh thu của shop để hiển thị **chi tiết phí sàn 5%** cho từng sản phẩm bán được.

---

## 🔧 Chi Tiết Thay Đổi

### 1. **Backend - shopController.js**

#### Hàm `getShopRevenue()`
- **Trước**: Chỉ hiển thị tổng doanh thu và tổng phí
- **Sau**: Hiển thị chi tiết từng sản phẩm + tổng hợp

**Dữ liệu trả về:**

```json
{
  "period": "month",
  "startDate": "2026-05-01",
  "summary": {
    "totalGrossRevenue": 1000000,      // Tổng tiền bán được
    "totalPlatformFees": 50000,        // Tổng phí sàn (5%)
    "totalNetRevenue": 950000,         // Tổng tiền thực nhận
    "totalOrders": 10,
    "totalProducts": 15,
    "platformFeeRate": 0.05            // Tỉ lệ phí 5%
  },
  "productDetails": [
    {
      "orderId": "...",
      "orderNumber": "ORD-123456",
      "productId": "...",
      "productName": "Sản phẩm A",
      "price": 100000,                 // Giá/item
      "quantity": 1,                   // Số lượng bán
      "grossAmount": 100000,           // Tổng tiền (giá × qty)
      "platformFeeRate": 0.05,         // 5%
      "platformFee": 5000,             // Phí sàn = 100000 × 5%
      "netAmount": 95000,              // Tiền thực nhận = 100000 - 5000
      "paidAt": "2026-05-10T...",
      "feeStatus": "pending|paid|unpaid"
    },
    // ... sản phẩm khác
  ],
  "policy": {...}
}
```

---

### 2. **Frontend - ShopRevenue.jsx**

#### Thay Đổi Giao Diện

1. **4 Card Thống Kê Chính:**
   - 💚 Tổng Tiền Bán (Green)
   - 🟨 Phí Sàn 5% (Amber)
   - 💚 Tổng Tiền Thực Nhận (Emerald)
   - 💙 Tỉ Lệ Phí (Blue)

2. **Bảng Chi Tiết Sản Phẩm:**
   | Cột | Nội Dung |
   |-----|---------|
   | Sản phẩm | Tên + hình ảnh + SKU |
   | Giá | Giá bán/item |
   | Số lượng | Số lượng bán |
   | Tổng tiền | Giá × Số lượng |
   | Phí sàn | 5% của tổng tiền |
   | Tiền thực nhận | Tổng tiền - Phí sàn |
   | Đơn hàng | Số đơn hàng |

3. **Hàng Tổng Cộng:**
   - Hiển thị tổng cộng: Tổng tiền, Phí sàn, Tiền thực nhận

---

## 💰 Ví Dụ Thực Tế

### Scenario: Shop bán 3 sản phẩm

| Sản phẩm | Giá | Qty | Tổng | Phí 5% | Thực nhận |
|---------|-----|-----|------|--------|-----------|
| Áo sơ mi | 100.000 | 2 | 200.000 | 10.000 | 190.000 |
| Quần | 150.000 | 1 | 150.000 | 7.500 | 142.500 |
| Giày | 500.000 | 1 | 500.000 | 25.000 | 475.000 |
| **TỔNG** | | | **850.000** | **42.500** | **807.500** |

**Giải thích:**
- Shop bán được tổng: **850.000đ**
- Phí sàn (5%): **42.500đ** ← Được trừ tự động từ ví shop
- Shop thực nhận: **807.500đ** ← Vào ví shop sau 3 ngày miễn phí

---

## 🔄 Quy Trình Tính Phí

1. **Customer thanh toán đơn hàng** ✓
   - Order status: "đã thanh toán"
   
2. **Hệ thống tính phí tự động** (sau 3 ngày miễn phí)
   - Phí sàn = Giá sản phẩm × 5%
   - Lưu vào `order.items[].platformFee`

3. **Trừ từ ví shop**
   - Nếu ví shop có đủ số dư → Tự trừ ngay
   - Nếu ví shop không đủ → Đánh dấu "unpaid", shop bị frozen

4. **Hiển thị trong ShopRevenue**
   - Tóm tắt: Tổng tiền bán, Phí sàn, Tiền thực nhận
   - Chi tiết: Từng sản phẩm, từng đơn hàng

---

## 📊 Tính Năng Hiện Tại

✅ Shop có thể:
- Tạo tài khoản shop
- Đăng ký sản phẩm
- Quản lý tồn kho sản phẩm
- Quản lý voucher
- **Quản lý doanh thu với chi tiết phí sàn** ← MỚI

✅ Quản lý doanh thu cho phép shop:
- Xem tổng tiền bán được (Gross Revenue)
- Xem chi tiết từng sản phẩm:
  - Giá bán
  - Số lượng
  - Tổng tiền (Gross Amount)
  - Phí sàn cụ thể (5%)
  - Tiền thực nhận (Net Amount)
- Xem tổng tiền thực nhận sau trừ phí

---

## 🛠️ Các File Được Sửa

| File | Thay Đổi |
|------|----------|
| `backend/controllers/shopController.js` | Sửa hàm `getShopRevenue()` để trả về chi tiết từng sản phẩm |
| `frontend/src/pages/ShopRevenue.jsx` | Sửa UI hiển thị bảng chi tiết phí sàn |

---

## 🧪 Test API

### Endpoint: GET `/api/shop/revenue?period=month`

**Response Example:**
```bash
curl -X GET http://localhost:5000/api/shop/revenue?period=month \
  -H "Authorization: Bearer <token>"
```

---

## 📝 Ghi Chú

- Phí sàn **5%** được tính trên giá sản phẩm
- Phí được tính ngay khi thanh toán thành công (sau 3 ngày miễn phí)
- Phí được trừ tự động từ ví shop
- Nếu ví không đủ, shop bị "frozen" (không được bán) cho đến khi nạp ví
- Mỗi sản phẩm có trạng thái phí: pending, paid, unpaid

---

**Ngày cập nhật:** May 14, 2026
**Status:** ✅ Hoàn thành
