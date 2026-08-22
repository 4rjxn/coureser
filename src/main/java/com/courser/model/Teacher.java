package com.courser.model;

import java.util.Objects;

/**
 * Represents a Teacher/Instructor in the Course Management System.
 * Inherits from User.
 */
public class Teacher extends User {
    private String teacherId;

    public Teacher(int userId, String userName, String name, String email, String teacherId) {
        super(userId, userName, name, email);
        setTeacherId(teacherId);
    }

    public Teacher(String userName, String name, String email, String teacherId) {
        super(0, userName, name, email);
        setTeacherId(teacherId);
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = Objects.requireNonNull(teacherId, "Teacher ID cannot be null").trim();
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "teacherId='" + teacherId + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}
