package com.courser.model;

public class User {
    int userId;
    String userName;
    String name;
    String email;

    User(int userId, String userName, String name, String email) {
        this.userId = userId;
        this.userName = userName;
        this.name = name;
        this.email = email;
    };

    public String getUserName() {
        return userName;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
