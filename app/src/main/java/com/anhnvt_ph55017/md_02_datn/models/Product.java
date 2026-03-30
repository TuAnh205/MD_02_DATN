package com.anhnvt_ph55017.md_02_datn.models;

import java.io.Serializable;

public class Product implements Serializable {

    String id;
    String name;
    double price;
    int image;          // local drawable resource id (for in-app DB items)
    String imageUrl;    // remote URL (for API products)
    String description;
    float rating;
    int reviewCount;
    int stock;
    boolean isFavorite = false;

    int qty = 1;
    boolean selected = true;
    
    String color = "Black";  // Default color
    int storage = 64;        // Default storage in GB

    public Product(int id, String name, double price, int image,
                   String description, int stock) {
        this.id = String.valueOf(id);
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
        this.stock = stock;
    }

    public Product(String id, String name, double price, String imageUrl,
                   String description, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.stock = stock;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
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

    public String getId() { return id; }

    public int getIntId() {
        try {
            return Integer.parseInt(id);
        } catch (Exception e) {
            return -1;
        }
    }

    public String getName() { return name; }

    public double getPrice() { return price; }

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }
}