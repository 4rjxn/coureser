package com.courser.model;

public class Student extends User {
    String studentId;
    static final int maxCredits = 20;
    int[] registeredCourses;

    public Student(int userId, String userName, String studentId, String name, String email,
            int[] registeredCourses) {
        super(userId, userName, name, email);
        this.studentId = studentId;
        this.registeredCourses = registeredCourses;
    }

    public String getStudentId() {
        return studentId;
    }

    public int getMaxCredits() {
        return maxCredits;
    }

}
