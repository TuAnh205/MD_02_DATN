package com.anhnvt_ph55017.md_02_datn.models;

import java.io.Serializable;

public class OrderItem implements Serializable {

    private String name;
    private double price;
    private int    quantity;
    private int    imageRes;
    private String imageUrl;   // URL ảnh từ API (có thể null)

    // ── Constructor cũ (SQLite) ───────────────────────────────────────────────
    public OrderItem(String name, double price, int quantity, int imageRes) {
        this.name     = name;
        this.price    = price;
        this.quantity = quantity;
        this.imageRes = imageRes;
        this.imageUrl = null;
    }

    // ── Constructor mới (API) — thêm imageUrl ────────────────────────────────
    public OrderItem(String name, double price, int quantity, int imageRes, String imageUrl) {
        this.name     = name;
        this.price    = price;
        this.quantity = quantity;
        this.imageRes = imageRes;
        this.imageUrl = imageUrl;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getName()     { return name; }
    public String getProductName() { return name; }
    public double getPrice()    { return price; }
    public int    getQuantity() { return quantity; }
    public int    getImageRes() { return imageRes; }
    public String getImageUrl() { return imageUrl; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setName(String name)         { this.name     = name; }
    public void setPrice(double price)       { this.price    = price; }
    public void setQuantity(int quantity)    { this.quantity = quantity; }
    public void setImageRes(int imageRes)    { this.imageRes = imageRes; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}