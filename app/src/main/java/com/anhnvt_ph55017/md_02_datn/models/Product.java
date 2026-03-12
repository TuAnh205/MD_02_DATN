package com.anhnvt_ph55017.md_02_datn.models;

import java.io.Serializable;

public class Product implements Serializable {

    int id;
    String name;
    double price;
    int image;
    String description;
    float rating;
    int reviewCount;
    int stock;
    boolean isFavorite = false;

    int qty = 1;
    boolean selected = true;

    public Product(int id, String name, double price, int image,
                   String description, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
        this.stock = stock;
    }

    public float getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public double getPrice() { return price; }

    public int getImage() { return image; }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    // ===== thêm =====

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}