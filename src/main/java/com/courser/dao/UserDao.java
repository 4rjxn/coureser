package com.courser.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.courser.model.Student;
import com.courser.model.User;
import com.courser.utils.Database;

public class UserDao {
    public static boolean addUser(User user) {
        Connection conn;
        String sql = """
                INSERT INTO users(username, name, email)
                VALUES (?, ?, ?)
                """;
        try {
            conn = Database.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getName());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    boolean udateUesr(User user) {
        // TODO: update user in database
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    boolean removeUser(User user) {
        // TODO: remove user from database
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public User[] search(String query) {
        // TODO: search user
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
