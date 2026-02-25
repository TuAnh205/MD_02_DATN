package com.anhnvt_ph55017.md_02_datn.models;

public class Product {

    int id;
    String name;
    double price;
    int image; // 🔥 PHẢI là int

    public Product(int id, String name, double price, int image) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getImage() { // ✅ KHÔNG ĐỎ
        return image;
    }
}