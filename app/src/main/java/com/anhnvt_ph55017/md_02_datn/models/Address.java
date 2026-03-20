package com.anhnvt_ph55017.md_02_datn.models;

import java.io.Serializable;

public class Address implements Serializable {

    private int     id;
    private int     userId;
    private String  name;
    private String  phone;
    private String  address;
    private boolean isDefault;

    public Address(int id, int userId, String name, String phone, String address, boolean isDefault) {
        this.id        = id;
        this.userId    = userId;
        this.name      = name;
        this.phone     = phone;
        this.address   = address;
        this.isDefault = isDefault;
    }

    public int     getId()         { return id; }
    public int     getUserId()     { return userId; }
    public String  getName()       { return name; }
    public String  getPhone()      { return phone; }
    public String  getAddress()    { return address; }
    public boolean isDefault()     { return isDefault; }

    public void setName(String name)          { this.name      = name; }
    public void setPhone(String phone)        { this.phone     = phone; }
    public void setAddress(String address)    { this.address   = address; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
}