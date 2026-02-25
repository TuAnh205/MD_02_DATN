package com.anhnvt_ph55017.md_02_datn.models;

public class Category {

    int id;
    String name;
    int icon; // 🔥 PHẢI là int

    public Category(int id, String name, int icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIcon() { // ✅ KHÔNG ĐỎ
        return icon;
    }
}
