package com.courser.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.courser.model.Course;
import com.courser.model.Registration;
import com.courser.model.Student;
import com.courser.utils.Database;

public class RegistrationDao {

    public static void addRegistration(Registration registration) throws SQLException {
        if (registration == null || registration.getStudent() == null || registration.getCourse() == null) {
            throw new IllegalArgumentException("Registration, Student, and Course cannot be null.");
        }

        String insertRegSql = """
                INSERT INTO registrations(student_id, course_code, registration_date, status)
                VALUES (?, ?, ?, ?);
                """;
        String syncRegCoursesSql = """
                INSERT OR IGNORE INTO reg_courses(user_id, course_code)
                SELECT user_id, ? FROM students WHERE student_id = ?;
                """;

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(insertRegSql)) {
                    stmt.setString(1, registration.getStudent().getStudentId());
                    stmt.setString(2, registration.getCourse().getCourseCode());
                    stmt.setString(3, registration.getRegistrationDate().toString());
                    stmt.setString(4, registration.getStatus());
                    stmt.executeUpdate();
                }

                try (Statement idStmt = conn.createStatement();
                     ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid();")) {
                    if (rs.next()) {
                        int regId = rs.getInt(1);
                        registration.setRegistrationId(regId);
                    }
                }

                try (PreparedStatement syncStmt = conn.prepareStatement(syncRegCoursesSql)) {
                    syncStmt.setString(1, registration.getCourse().getCourseCode());
                    syncStmt.setString(2, registration.getStudent().getStudentId());
                    syncStmt.executeUpdate();
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

    public static boolean dropRegistration(String studentId, String courseCode) throws SQLException {
        if (studentId == null || courseCode == null) return false;

        String updateRegSql = """
                UPDATE registrations
                SET status = 'DROPPED'
                WHERE student_id = ? AND course_code = ? AND status = 'ACTIVE';
                """;
        String deleteRegCoursesSql = """
                DELETE FROM reg_courses
                WHERE user_id = (SELECT user_id FROM students WHERE student_id = ?)
                  AND course_code = ?;
                """;

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int affectedRows;
                try (PreparedStatement stmt = conn.prepareStatement(updateRegSql)) {
                    stmt.setString(1, studentId.trim());
                    stmt.setString(2, courseCode.trim().toUpperCase());
                    affectedRows = stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(deleteRegCoursesSql)) {
                    stmt.setString(1, studentId.trim());
                    stmt.setString(2, courseCode.trim().toUpperCase());
                    stmt.executeUpdate();
                }

                conn.commit();
                return affectedRows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static boolean isStudentRegistered(String studentId, String courseCode) {
        if (studentId == null || courseCode == null) return false;
        String sql = "SELECT COUNT(*) FROM registrations WHERE student_id = ? AND course_code = ? AND status = 'ACTIVE';";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId.trim());
            stmt.setString(2, courseCode.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static List<Course> getRegisteredCoursesForStudent(String studentId) {
        List<Course> courses = new ArrayList<>();
        if (studentId == null || studentId.isBlank()) return courses;

        String sql = """
                SELECT c.course_code, c.title, c.credits, c.instructor_name, c.capacity
                FROM courses c
                JOIN registrations r ON c.course_code = r.course_code
                WHERE r.student_id = ? AND r.status = 'ACTIVE'
                ORDER BY c.course_code;
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("course_code");
                    int capacity = rs.getInt("capacity");
                    if (capacity <= 0) capacity = 30;
                    List<String> prereqs = CourseDao.getPrerequisites(code);
                    int enrolled = CourseDao.getEnrolledCount(code);
                    courses.add(new Course(
                            code,
                            rs.getString("title"),
                            rs.getInt("credits"),
                            rs.getString("instructor_name"),
                            prereqs,
                            capacity,
                            enrolled
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("RegistrationDao.getRegisteredCoursesForStudent error: " + e.getMessage());
        }
        return courses;
    }

    public static List<Registration> getRegistrationsForStudent(String studentId) {
        List<Registration> list = new ArrayList<>();
        if (studentId == null || studentId.isBlank()) return list;

        String sql = """
                SELECT r.registration_id, r.student_id, r.course_code, r.registration_date, r.status
                FROM registrations r
                WHERE r.student_id = ? AND r.status = 'ACTIVE'
                ORDER BY r.registration_date DESC;
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                Student student = UserDao.getStudentByStudentId(studentId);
                while (rs.next()) {
                    int id = rs.getInt("registration_id");
                    String courseCode = rs.getString("course_code");
                    LocalDate date = LocalDate.parse(rs.getString("registration_date"));
                    String status = rs.getString("status");
                    Course course = CourseDao.getCourseByCode(courseCode);
                    if (student != null && course != null) {
                        list.add(new Registration(id, student, course, date, status));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("RegistrationDao.getRegistrationsForStudent error: " + e.getMessage());
        }
        return list;
    }

    public static List<Registration> getRegistrationsForCourse(String courseCode) {
        List<Registration> list = new ArrayList<>();
        if (courseCode == null || courseCode.isBlank()) return list;

        String sql = """
                SELECT r.registration_id, r.student_id, r.course_code, r.registration_date, r.status
                FROM registrations r
                WHERE r.course_code = ? AND r.status = 'ACTIVE'
                ORDER BY r.registration_date DESC;
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                Course course = CourseDao.getCourseByCode(courseCode);
                while (rs.next()) {
                    int id = rs.getInt("registration_id");
                    String studentId = rs.getString("student_id");
                    LocalDate date = LocalDate.parse(rs.getString("registration_date"));
                    String status = rs.getString("status");
                    Student student = UserDao.getStudentByStudentId(studentId);
                    if (student != null && course != null) {
                        list.add(new Registration(id, student, course, date, status));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("RegistrationDao.getRegistrationsForCourse error: " + e.getMessage());
        }
        return list;
    }

    public static List<Registration> getAllRegistrations() {
        return searchRegistrations("", Integer.MAX_VALUE, 0);
    }

    public static List<Registration> searchRegistrations(String query, int limit, int offset) {
        List<Registration> list = new ArrayList<>();
        String sql = """
                SELECT r.registration_id, r.student_id, r.course_code, r.registration_date, r.status,
                       u.user_id, u.username, u.name, u.email, s.max_credits,
                       c.title, c.credits, c.instructor_name, c.capacity
                FROM registrations r
                JOIN students s ON r.student_id = s.student_id
                JOIN users u ON s.user_id = u.user_id
                JOIN courses c ON r.course_code = c.course_code
                WHERE r.status = 'ACTIVE'
                  AND (r.student_id LIKE ? OR u.name LIKE ? OR r.course_code LIKE ? OR c.title LIKE ?)
                ORDER BY r.registration_id DESC
                LIMIT ? OFFSET ?;
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + (query != null ? query.trim() : "") + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            stmt.setInt(5, limit);
            stmt.setInt(6, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int regId = rs.getInt("registration_id");
                    String sId = rs.getString("student_id");
                    String cCode = rs.getString("course_code");
                    LocalDate regDate = LocalDate.parse(rs.getString("registration_date"));
                    String status = rs.getString("status");

                    Student student = new Student(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            sId,
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getInt("max_credits")
                    );

                    Course course = new Course(
                            cCode,
                            rs.getString("title"),
                            rs.getInt("credits"),
                            rs.getString("instructor_name"),
                            CourseDao.getPrerequisites(cCode),
                            rs.getInt("capacity"),
                            CourseDao.getEnrolledCount(cCode)
                    );

                    list.add(new Registration(regId, student, course, regDate, status));
                }
            }
        } catch (Exception e) {
            System.err.println("RegistrationDao.searchRegistrations error: " + e.getMessage());
        }
        return list;
    }

    public static int getRegistrationCount(String query) {
        String sql = """
                SELECT COUNT(*)
                FROM registrations r
                JOIN students s ON r.student_id = s.student_id
                JOIN users u ON s.user_id = u.user_id
                JOIN courses c ON r.course_code = c.course_code
                WHERE r.status = 'ACTIVE'
                  AND (r.student_id LIKE ? OR u.name LIKE ? OR r.course_code LIKE ? OR c.title LIKE ?);
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + (query != null ? query.trim() : "") + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("RegistrationDao.getRegistrationCount error: " + e.getMessage());
        }
        return 0;
    }
}
