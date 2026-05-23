package com.anhnvt_ph55017.md_02_datn.models;

import java.util.List;

public class Order {
    String imageUrl;
    String orderCode;
    String customerName;
    String itemSummary;
    String productImageUrl;

    String id;
    String date;
    double total;
    String status;  // PENDING, PROCESSING, SHIPPING, DELIVERED, CANCELLED
    String arrivalDate;
    int itemCount;
    int imageRes;

    // additional product info for detail view
    String productName;
    double productPrice;
    String productDesc;

    // new fields
    String shippingAddress;
    List<OrderItem> items;
    String paymentMethod;
    String paymentStatus;
    double voucherDiscount;
    String createdAt;  // Để tính ngày dự kiến nhận
    String cancellationReason;  // Lý do hủy đơn
    double rating;  // Đánh giá sao (0-5)
    String reviewComment;  // Review comment từ khách
    String reviewedAt;  // Ngày đánh giá

    public Order(String id, String date, double total, String status, String arrivalDate, int itemCount, int imageRes) {
        this(id, date, total, status, arrivalDate, itemCount, imageRes, "", 0, "", "");
    }

    public Order(String id, String date, double total, String status, String arrivalDate, int itemCount, int imageRes,
                 String productName, double productPrice, String productDesc, String imageUrl) {
        this.id = id;
        this.date = date;
        this.total = total;
        this.status = status;
        this.arrivalDate = arrivalDate;
        this.itemCount = itemCount;
        this.imageRes = imageRes;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productDesc = productDesc;
        this.shippingAddress = "";
        this.items = null;
        this.paymentMethod = "Thanh toán khi nhận hàng";
        this.voucherDiscount = 0;
        this.imageUrl = imageUrl;
    }

    // new constructor with address and items
    public Order(String id, String date, double total, String status, String arrivalDate, int itemCount,
                 String shippingAddress, List<OrderItem> items, String imageUrl) {
        this.id = id;
        this.date = date;
        this.total = total;
        this.status = status;
        this.arrivalDate = arrivalDate;
        this.itemCount = itemCount;
        this.imageRes = 0;
        this.productName = "";
        this.productPrice = 0;
        this.productDesc = "";
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.paymentMethod = "Thanh toán khi nhận hàng";
        this.voucherDiscount = 0;
        this.imageUrl = imageUrl;
    }

    // new constructor with payment method
    public Order(String id, String date, double total, String status, String arrivalDate, int itemCount,
                 String shippingAddress, List<OrderItem> items, String paymentMethod, String imageUrl) {
        this.id = id;
        this.date = date;
        this.total = total;
        this.status = status;
        this.arrivalDate = arrivalDate;
        this.itemCount = itemCount;
        this.imageRes = 0;
        this.productName = "";
        this.productPrice = 0;
        this.productDesc = "";
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Thanh toán khi nhận hàng";
        this.voucherDiscount = 0;
        this.imageUrl = imageUrl;
    }

    public Order(String id, String orderCode, String formattedDate, double total, String status,
                 String customerName, String itemSummary, String productImageUrl, int itemCount) {
        this.id = id;
        this.orderCode = orderCode;
        this.date = formattedDate;
        this.total = total;
        this.status = status;
        this.customerName = customerName;
        this.itemSummary = itemSummary;
        this.productImageUrl = productImageUrl;
        this.itemCount = itemCount;
        this.imageRes = 0;
        this.productName = "";
        this.productPrice = 0;
        this.productDesc = "";
        this.shippingAddress = "";
        this.items = null;
        this.paymentMethod = "Thanh toán khi nhận hàng";
        this.imageUrl = productImageUrl;
    }

    public String getOrderCode() {
        if (orderCode != null && !orderCode.isEmpty()) return orderCode;
        return id != null ? id : "";
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getFormattedDate() {
        if (date == null || date.isEmpty()) return "";
        try {
            if (date.contains("T")) {
                String[] parts = date.split("T");
                String datePart = parts[0];
                String timePart = parts.length > 1 ? parts[1].substring(0, Math.min(5, parts[1].length())) : "00:00";
                String[] dateParts = datePart.split("-");
                if (dateParts.length == 3) {
                    return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0] + " • " + timePart;
                }
            }
        } catch (Exception ignored) {
        }
        return date;
    }

    public String getCustomerName() {
        if (customerName != null && !customerName.isEmpty()) return customerName;
        if (productName != null && !productName.isEmpty()) return productName;
        return "";
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getItemSummary() {
        if (itemSummary != null && !itemSummary.isEmpty()) return itemSummary;
        if (items != null && !items.isEmpty()) {
            String firstName = items.get(0).getProductName();
            if (items.size() == 1) {
                return firstName;
            }
            return firstName + " và " + (items.size() - 1) + " sản phẩm khác";
        }
        return itemCount + " sản phẩm";
    }

    public void setItemSummary(String itemSummary) {
        this.itemSummary = itemSummary;
    }

    public String getProductImageUrl() {
        if (productImageUrl != null && !productImageUrl.isEmpty()) return productImageUrl;
        return imageUrl != null ? imageUrl : "";
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(String arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public double getVoucherDiscount() {
        return voucherDiscount;
    }

    public void setVoucherDiscount(double voucherDiscount) {
        this.voucherDiscount = voucherDiscount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
