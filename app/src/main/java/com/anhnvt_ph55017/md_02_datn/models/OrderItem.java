package com.anhnvt_ph55017.md_02_datn.models;

public class OrderItem {
    private String productName;
    private double price;
    private int quantity;
    private int imageRes;

    public OrderItem(String productName, double price, int quantity, int imageRes) {
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imageRes = imageRes;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public double getTotal() {
        return price * quantity;
    }
}