package com.courser.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.courser.model.Course;
import com.courser.model.Registrar;
import com.courser.model.Student;
import com.courser.model.User;
import com.courser.utils.Database;

public class UserDao {

    public static void addStudent(Student student) throws SQLException {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        String userAddSql = """
                INSERT INTO users(username, name, email)
                VALUES (?, ?, ?);
                """;
        String studentAddSql = """
                INSERT INTO students(user_id, student_id, max_credits)
                VALUES (?, ?, ?);
                """;
        String completedCourseSql = """
                INSERT OR IGNORE INTO completed_courses(student_id, course_code, completion_date)
                VALUES (?, ?, ?);
                """;

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement userStmt = conn.prepareStatement(userAddSql)) {
                    userStmt.setString(1, student.getUserName());
                    userStmt.setString(2, student.getName());
                    userStmt.setString(3, student.getEmail());
                    userStmt.executeUpdate();
                }

                try (Statement idStmt = conn.createStatement();
                        ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid();")) {
                    if (!rs.next()) {
                        throw new SQLException("Failed to get generated user id.");
                    }
                    userId = rs.getInt(1);
                    student.setUserId(userId);
                }

                try (PreparedStatement studentStmt = conn.prepareStatement(studentAddSql)) {
                    studentStmt.setInt(1, userId);
                    studentStmt.setString(2, student.getStudentId());
                    studentStmt.setInt(3, student.getMaxCredits());
                    studentStmt.executeUpdate();
                }

                // Add any completed courses
                Set<String> completed = student.getCompletedCourses();
                if (completed != null && !completed.isEmpty()) {
                    try (PreparedStatement compStmt = conn.prepareStatement(completedCourseSql)) {
                        for (String c : completed) {
                            compStmt.setString(1, student.getStudentId());
                            compStmt.setString(2, c.trim().toUpperCase());
                            compStmt.setString(3, LocalDate.now().toString());
                            compStmt.executeUpdate();
                        }
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static void updateStudent(Student student) throws SQLException {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        String userSql = """
                UPDATE users
                SET username = ?, name = ?, email = ?
                WHERE user_id = ?;
                """;
        String studentSql = """
                UPDATE students
                SET student_id = ?, max_credits = ?
                WHERE user_id = ?;
                """;

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                    userStmt.setString(1, student.getUserName());
                    userStmt.setString(2, student.getName());
                    userStmt.setString(3, student.getEmail());
                    userStmt.setInt(4, student.getUserId());
                    userStmt.executeUpdate();
                }

                try (PreparedStatement studentStmt = conn.prepareStatement(studentSql)) {
                    studentStmt.setString(1, student.getStudentId());
                    studentStmt.setInt(2, student.getMaxCredits());
                    studentStmt.setInt(3, student.getUserId());
                    studentStmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static boolean removeStudentByStudentId(String studentId) throws SQLException {
        if (studentId == null || studentId.isBlank())
            return false;
        String findUserIdSql = "SELECT user_id FROM students WHERE student_id = ?;";
        try (Connection conn = Database.getConnection()) {
            int userId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(findUserIdSql)) {
                stmt.setString(1, studentId.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    }
                }
            }
            if (userId != -1) {
                return removeStudent(userId);
            }
        }
        return false;
    }

    public static boolean removeStudent(int userId) throws SQLException {
        String deleteRegSql = "DELETE FROM registrations WHERE student_id = (SELECT student_id FROM students WHERE user_id = ?);";
        String deleteCompletedSql = "DELETE FROM completed_courses WHERE student_id = (SELECT student_id FROM students WHERE user_id = ?);";
        String deleteRegCoursesSql = "DELETE FROM reg_courses WHERE user_id = ?;";
        String deleteStudentSql = "DELETE FROM students WHERE user_id = ?;";
        String deleteUserSql = "DELETE FROM users WHERE user_id = ?;";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(deleteRegSql)) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(deleteCompletedSql)) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(deleteRegCoursesSql)) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(deleteStudentSql)) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                int affected;
                try (PreparedStatement stmt = conn.prepareStatement(deleteUserSql)) {
                    stmt.setInt(1, userId);
                    affected = stmt.executeUpdate();
                }
                conn.commit();
                return affected > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static Student[] searchStudent(String query, int limit, int offset) throws SQLException {
        String sqlQuery = """
                SELECT u.user_id, u.username, u.name, u.email, s.student_id, s.max_credits
                FROM users u
                JOIN students s ON u.user_id = s.user_id
                WHERE s.student_id LIKE ? OR u.name LIKE ?
                ORDER BY u.name
                LIMIT ? OFFSET ?;
                """;
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            String pattern = "%" + (query != null ? query.trim() : "") + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Student> students = new ArrayList<>();
                while (rs.next()) {
                    String sId = rs.getString("student_id");
                    Student student = new Student(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            sId,
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getInt("max_credits"));
                    List<Course> regCourses = RegistrationDao.getRegisteredCoursesForStudent(sId);
                    student.setRegisteredCourses(regCourses);

                    Set<String> completed = getCompletedCourseCodes(sId);
                    for (String comp : completed) {
                        student.addCompletedCourse(comp);
                    }

                    students.add(student);
                }
                return students.toArray(new Student[0]);
            }
        }
    }

    public static List<Student> searchAllStudents(String query) {
        String sqlQuery = """
                SELECT u.user_id, u.username, u.name, u.email, s.student_id, s.max_credits
                FROM users u
                JOIN students s ON u.user_id = s.user_id
                WHERE s.student_id LIKE ? OR u.name LIKE ?
                ORDER BY u.name;
                """;
        List<Student> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            String pattern = "%" + (query != null ? query.trim() : "") + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String sId = rs.getString("student_id");
                    Student student = new Student(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            sId,
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getInt("max_credits"));
                    List<Course> regCourses = RegistrationDao.getRegisteredCoursesForStudent(sId);
                    student.setRegisteredCourses(regCourses);

                    Set<String> completed = getCompletedCourseCodes(sId);
                    for (String comp : completed) {
                        student.addCompletedCourse(comp);
                    }
                    list.add(student);
                }
            }
        } catch (Exception e) {
            System.err.println("UserDao.searchAllStudents error: " + e.getMessage());
        }
        return list;
    }

    public static Student getStudentFromUserId(int id) throws SQLException {
        String sqlQuery = """
                SELECT u.user_id, u.username, u.name, u.email, s.student_id, s.max_credits
                FROM users u
                JOIN students s ON u.user_id = s.user_id
                WHERE u.user_id = ?;
                """;
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String sId = rs.getString("student_id");
                    Student student = new Student(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            sId,
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getInt("max_credits"));
                    List<Course> regCourses = RegistrationDao.getRegisteredCoursesForStudent(sId);
                    student.setRegisteredCourses(regCourses);

                    Set<String> completed = getCompletedCourseCodes(sId);
                    for (String comp : completed) {
                        student.addCompletedCourse(comp);
                    }
                    return student;
                }
            }
        }
        return null;
    }

    public static Student getStudentByStudentId(String studentId) {
        if (studentId == null || studentId.isBlank())
            return null;
        String sqlQuery = """
                SELECT u.user_id, u.username, u.name, u.email, s.student_id, s.max_credits
                FROM users u
                JOIN students s ON u.user_id = s.user_id
                WHERE s.student_id = ?;
                """;
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            stmt.setString(1, studentId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String sId = rs.getString("student_id");
                    Student student = new Student(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            sId,
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getInt("max_credits"));
                    List<Course> regCourses = RegistrationDao.getRegisteredCoursesForStudent(sId);
                    student.setRegisteredCourses(regCourses);

                    Set<String> completed = getCompletedCourseCodes(sId);
                    for (String comp : completed) {
                        student.addCompletedCourse(comp);
                    }
                    return student;
                }
            }
        } catch (Exception e) {
            System.err.println("UserDao.getStudentByStudentId error: " + e.getMessage());
        }
        return null;
    }

    public static void addCompletedCourse(String studentId, String courseCode) throws SQLException {
        if (studentId == null || courseCode == null)
            return;
        String sql = """
                INSERT OR IGNORE INTO completed_courses(student_id, course_code, completion_date)
                VALUES (?, ?, ?);
                """;
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId.trim());
            stmt.setString(2, courseCode.trim().toUpperCase());
            stmt.setString(3, LocalDate.now().toString());
            stmt.executeUpdate();
        }
    }

    public static void removeCompletedCourse(String studentId, String courseCode) throws SQLException {
        if (studentId == null || courseCode == null)
            return;
        String sql = "DELETE FROM completed_courses WHERE student_id = ? AND course_code = ?;";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId.trim());
            stmt.setString(2, courseCode.trim().toUpperCase());
            stmt.executeUpdate();
        }
    }

    public static Set<String> getCompletedCourseCodes(String studentId) {
        Set<String> set = new HashSet<>();
        if (studentId == null || studentId.isBlank())
            return set;
        String sql = "SELECT course_code FROM completed_courses WHERE student_id = ?;";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getString("course_code").toUpperCase());
                }
            }
        } catch (Exception e) {
            System.err.println("UserDao.getCompletedCourseCodes error: " + e.getMessage());
        }
        return set;
    }

    public static void addRegistrar(Registrar registrar) throws SQLException {
        if (registrar == null)
            return;
        String userAddSql = "INSERT INTO users(username, name, email) VALUES (?, ?, ?);";
        String regAddSql = "INSERT INTO registrars(user_id, registrar_id, department) VALUES (?, ?, ?);";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement userStmt = conn.prepareStatement(userAddSql)) {
                    userStmt.setString(1, registrar.getUserName());
                    userStmt.setString(2, registrar.getName());
                    userStmt.setString(3, registrar.getEmail());
                    userStmt.executeUpdate();
                }
                try (Statement idStmt = conn.createStatement();
                        ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid();")) {
                    if (!rs.next())
                        throw new SQLException("Failed to get registrar user id.");
                    userId = rs.getInt(1);
                    registrar.setUserId(userId);
                }
                try (PreparedStatement regStmt = conn.prepareStatement(regAddSql)) {
                    regStmt.setInt(1, userId);
                    regStmt.setString(2, registrar.getRegistrarId());
                    regStmt.setString(3, registrar.getDepartment());
                    regStmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static int getTotalStudentCount() {
        String sql = "SELECT COUNT(*) FROM students;";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception ignored) {
        }
        return 0;
    }
}
