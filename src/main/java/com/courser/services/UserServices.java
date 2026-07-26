package com.courser.services;

import com.courser.dao.UserDao;
import com.courser.model.Student;

public class UserServices {
    public static void registerNewStudent(Student student) {
        try {
            UserDao.addStudent(student);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
