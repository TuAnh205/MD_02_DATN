package com.anhnvt_ph55017.md_02_datn.models;

public class Address {

    String id;
    String userId;
    String name;
    String phone;
    String address;
    String city;
    String district;
    String ward;
    boolean isDefault;

    public Address(String id, String userId, String name, String phone, String address, String city, String district, String ward, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.district = district;
        this.ward = ward;
        this.isDefault = isDefault;
    }

    // Constructor cũ để không lỗi code cũ
    public Address(String id, String userId, String name, String phone, String address, boolean isDefault) {
        this(id, userId, name, phone, address, "", "", "", isDefault);
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getDistrict() { return district; }
    public String getWard() { return ward; }
    public boolean isDefault() { return isDefault; }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}