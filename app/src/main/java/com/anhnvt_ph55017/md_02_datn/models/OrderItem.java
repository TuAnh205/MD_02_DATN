package com.anhnvt_ph55017.md_02_datn.models;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private String productName;
    private double price;
    private int quantity;
    private int imageRes;
    private String imageUrl;

    public OrderItem(String productName, double price, int quantity, int imageRes) {
        this(productName, price, quantity, imageRes, null);
    }

    public OrderItem(String productName, double price, int quantity, int imageRes, String imageUrl) {
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imageRes = imageRes;
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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