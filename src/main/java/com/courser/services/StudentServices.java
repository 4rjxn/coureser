package com.courser.services;

import com.courser.dao.UserDao;
import com.courser.model.Student;

public class StudentServices {
    public static void registerNewStudent(Student student, String[] courseCodes) {
        try {
            UserDao.addStudent(student);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static Student[] searchStudents(String query, int limit, int offset) {
        try {
            return UserDao.searchStudent(query, limit, offset);
        } catch (Exception e) {
            return null;
        }
    }

}
