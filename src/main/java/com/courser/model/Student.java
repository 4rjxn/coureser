package com.courser.model;

public class Student extends User {
    int studentId;
    int maxCredits;
    int[] registeredCourses;

    public Student(int userId, String userName, int studentId, String name, String email, int maxCredits,
            int[] registeredCourses) {
        super(userId, userName, name, email);
    }
}
