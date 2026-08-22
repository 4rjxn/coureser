package com.courser.services;

import com.courser.model.Student;

public class UserServices {
    public static void registerNewStudent(Student student) {
        StudentServices.registerNewStudent(student, null);
    }
}
