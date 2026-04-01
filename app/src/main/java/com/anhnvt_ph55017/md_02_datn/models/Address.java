package com.anhnvt_ph55017.md_02_datn.models;

public class Address {

    String id;
    String userId;
    String name;
    String phone;
    String address;
    boolean isDefault;

    public Address(String id, String userId, String name, String phone, String address, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public boolean isDefault() { return isDefault; }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}