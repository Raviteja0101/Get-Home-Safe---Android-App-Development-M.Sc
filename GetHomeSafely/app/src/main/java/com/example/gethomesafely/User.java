package com.example.gethomesafely;

public class User {
    private String name;
    private String email;
    private String phone;

    public User() {
        name = null;
        email = null;
        phone = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
