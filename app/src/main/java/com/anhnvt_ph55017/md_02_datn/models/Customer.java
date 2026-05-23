package com.anhnvt_ph55017.md_02_datn.models;

import java.io.Serializable;

public class Customer implements Serializable {
    private String id;
    private String name;
    private String email;
    private String phone;
    private int orderCount;

    public Customer(String id, String name, String email, String phone, int orderCount) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.orderCount = orderCount;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public int getOrderCount() { return orderCount; }

    public String getInitials() {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split(" ");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
