package com.anhnvt_ph55017.md_02_datn.models;

public class User {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String password;

    public User(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    // getter
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
}
