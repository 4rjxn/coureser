package com.courser.model;

import java.util.Objects;

/**
 * Base class representing a user in the course management system.
 * Demonstrates encapsulation and serves as the foundation for inheritance (Student, Registrar, Teacher).
 */
public class User {
    private int userId;
    private String userName;
    private String name;
    private String email;

    public User(int userId, String userName, String name, String email) {
        this.userId = userId;
        this.userName = Objects.requireNonNull(userName, "Username cannot be null").trim();
        this.name = Objects.requireNonNull(name, "Name cannot be null").trim();
        this.email = Objects.requireNonNull(email, "Email cannot be null").trim();
    }

    public User(String userName, String name, String email) {
        this(0, userName, name, email);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = Objects.requireNonNull(userName, "Username cannot be null").trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null").trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email, "Email cannot be null").trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId || (userName != null && userName.equalsIgnoreCase(user.userName));
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName != null ? userName.toLowerCase() : userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
