package com.courser.services;

import com.courser.dao.UserDao;
import com.courser.model.User;

public class UserServices {
    public static void registerNewUser(User user) {
        UserDao.addUser(user);
    }
}
