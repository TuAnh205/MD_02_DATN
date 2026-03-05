package com.anhnvt_ph55017.md_02_datn.models;

public class Product {
    int id;
    String name;
    double price;
    int image;
    String description;
    float rating;
    int reviewCount;
    int stock;
    boolean isFavorite = false;
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
}