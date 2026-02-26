package com.anhnvt_ph55017.md_02_datn.models;

public class Category {
    int id;
    String name;
    int image;

    public Category(int id, String name, int image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getImage() { return image; }
}
