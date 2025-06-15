package com.example.app.data;

public class User {
    private String id;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String profilePhotoUrl;

    public User() {} // Обязательный пустой конструктор

    public User(String id, String name, String surname, String email, String phone, String profilePhotoUrl) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return this.surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePhotoUrl() {
        return this.profilePhotoUrl;
    }

    public void set(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }





}