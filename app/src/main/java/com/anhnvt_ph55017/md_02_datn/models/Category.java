package com.anhnvt_ph55017.md_02_datn.models;

public class Category {
    private String id;
    private String name;
    private int image;
    private int productCount;

    // Constructor for old way
    public Category(int id, String name, int image) {
        this.name = name;
        this.image = image;
        this.productCount = 0;
    }

    // Constructor for API response (MongoDB)
    public Category(String id, String name, int productCount) {
        this.id = id;
        this.name = name;
        this.productCount = productCount;
        this.image = 0;
    }

    // Full constructor
    public Category(String id, String name, int image, int productCount) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.productCount = productCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }
}
