package com.anhnvt_ph55017.md_02_datn.models;

import java.util.List;

public class Order {

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

    public Order(String id, String date, double total, String status, String arrivalDate, int itemCount, int imageRes) {
        this(id, date, total, status, arrivalDate, itemCount, imageRes, "", 0, "");
    }

    public Order(String id, String date, double total, String status, String arrivalDate, int itemCount, int imageRes,
                 String productName, double productPrice, String productDesc) {
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
    }

    // new constructor with address and items
    public Order(String id, String date, double total, String status, String arrivalDate, int itemCount,
                 String shippingAddress, List<OrderItem> items) {
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
    }

    // new constructor with payment method
    public Order(String id, String date, double total, String status, String arrivalDate, int itemCount,
                 String shippingAddress, List<OrderItem> items, String paymentMethod) {
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
}
