package com.courser.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.courser.model.Student;
import com.courser.model.User;
import com.courser.utils.Database;

public class UserDao {
    public static void addStudent(Student student) throws SQLException {
        String userAddSql = """
                INSERT INTO users(username, name, email)
                VALUES (?, ?, ?)
                RETURNING user_id
                """;
        String studentAddSql = """
                INSERT INTO students(user_id,student_id,max_credits)
                VALUES (?,?,?)
                """;
        Connection conn = Database.getConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement userStmt = conn.prepareStatement(userAddSql);
            userStmt.setString(1, student.getUserName());
            userStmt.setString(2, student.getName());
            userStmt.setString(3, student.getEmail());
            ResultSet rs = userStmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Failed to get user id.");
            }
            int userId = rs.getInt(1);
            rs.close();
            PreparedStatement studentStmt = conn.prepareStatement(studentAddSql);

            studentStmt.setInt(1, userId);
            studentStmt.setString(2, student.getStudentId());
            studentStmt.setInt(3, student.getMaxCredits());

            studentStmt.executeUpdate();

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
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
