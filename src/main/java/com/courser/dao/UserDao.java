package com.courser.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
            userStmt.close();

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

    public static void updateStudent(Student student) throws SQLException {
        String userSql = """
                UPDATE users
                SET
                    username = ?,
                    name = ?,
                    email = ?
                WHERE user_id = ?;
                """;
        String studentSql = """
                UPDATE students
                SET
                    student_id = ?,
                    max_credits = ?
                WHERE user_id = ?;
                """;
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (
                    PreparedStatement userStmt = conn.prepareStatement(userSql);
                    PreparedStatement studentStmt = conn.prepareStatement(studentSql)) {

                userStmt.setString(1, student.getUserName());
                userStmt.setString(2, student.getName());
                userStmt.setString(3, student.getEmail());
                userStmt.setInt(4, student.getUserId());

                userStmt.executeUpdate();

                studentStmt.setString(1, student.getStudentId());
                studentStmt.setInt(2, student.getMaxCredits());
                studentStmt.setInt(3, student.getUserId());

                studentStmt.executeUpdate();

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    boolean removeUser(User user) {
        // TODO: remove user from database
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static Student[] searchStudent(String query, int limit, int offset) throws SQLException {
        String sqlQuery = """
                SELECT u.user_id, u.username, u.name, u.email,s.student_id,s.max_credits
                FROM users u
                JOIN students s
                    ON u.user_id = s.user_id
                WHERE s.student_id LIKE ?
                    OR u.name LIKE ?
                ORDER BY u.name
                LIMIT ? OFFSET ?;
                """;
        Connection conn = Database.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setString(1, query + "%");
            stmt.setString(2, query + "%");
            stmt.setString(3, String.valueOf(limit));
            stmt.setString(4, String.valueOf(offset));
            ResultSet rs = stmt.executeQuery();
            List<Student> students = new ArrayList<>();
            while (rs.next()) {
                Student student = new Student(rs.getInt("user_id"), rs.getString("username"),
                        rs.getString("student_id"), rs.getString("name"),
                        rs.getString("email"), Integer.valueOf(rs.getString("max_credits")), null);
                students.add(student);
            }
            stmt.close();
            return students.toArray(new Student[0]);

        } catch (Exception e) {
            System.out.println("Search Exception");
            throw e;
        }
    }

    public static Student getStudentFromUserId(int id) throws SQLException {
        String sqlQuery = """
                SELECT u.user_id, u.username, u.name, u.email,s.student_id,s.max_credits
                FROM users u
                JOIN students s
                    ON u.user_id = s.user_id
                WHERE u.user_id = ?;
                """;
        Connection conn = Database.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setString(1, String.valueOf(id));
            ResultSet rs = stmt.executeQuery();
            Student student;
            if (rs.next()) {
                student = new Student(rs.getInt("user_id"), rs.getString("username"),
                        rs.getString("student_id"), rs.getString("name"),
                        rs.getString("email"), Integer.valueOf(rs.getString("max_credits")), null);
            } else {
                return null;
            }
            stmt.close();
            return student;

        } catch (Exception e) {
            System.out.println("Search Exception");
            throw e;
        }
    }
}
