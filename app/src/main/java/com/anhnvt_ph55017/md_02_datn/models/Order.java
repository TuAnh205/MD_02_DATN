package com.anhnvt_ph55017.md_02_datn.models;

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
}
