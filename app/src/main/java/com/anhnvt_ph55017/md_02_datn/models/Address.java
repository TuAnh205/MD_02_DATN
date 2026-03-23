package com.anhnvt_ph55017.md_02_datn.models;

public class Address {

    int id;
    int userId;
    String name;
    String phone;
    String address;
    boolean isDefault;

    public Address(int id, int userId, String name, String phone, String address, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.isDefault = isDefault;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}