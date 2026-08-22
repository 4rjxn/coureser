package com.courser.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.courser.model.Course;
import com.courser.utils.Database;

public class CourseDao {

    public static void addNew(Course course) throws SQLException {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        String courseSql = """
                INSERT INTO courses(course_code, title, credits, instructor_name, capacity)
                VALUES (?, ?, ?, ?, ?)
                """;
        String preReqSql = """
                INSERT OR IGNORE INTO course_prereq(course_code, prerequisite_id)
                VALUES (?, ?)
                """;

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(courseSql)) {
                    stmt.setString(1, course.getCourseCode());
                    stmt.setString(2, course.getTitle());
                    stmt.setInt(3, course.getCredits());
                    stmt.setString(4, course.getInstructorName());
                    stmt.setInt(5, course.getCapacity());
                    stmt.executeUpdate();
                }

                String[] codes = course.getPrerequisiteCourses();
                if (codes != null && codes.length > 0) {
                    try (PreparedStatement query = conn.prepareStatement(preReqSql)) {
                        for (String code : codes) {
                            if (code != null && !code.isBlank()) {
                                query.setString(1, course.getCourseCode());
                                query.setString(2, code.trim().toUpperCase());
                                query.executeUpdate();
                            }
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

    public static boolean removeCourse(String courseCode) throws SQLException {
        if (courseCode == null || courseCode.isBlank()) {
            return false;
        }
        String deletePrereqSql = "DELETE FROM course_prereq WHERE course_code = ? OR prerequisite_id = ?";
        String deleteRegSql = "DELETE FROM registrations WHERE course_code = ?";
        String deleteRegCoursesSql = "DELETE FROM reg_courses WHERE course_code = ?";
        String deleteCourseSql = "DELETE FROM courses WHERE course_code = ?";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(deletePrereqSql)) {
                    stmt.setString(1, courseCode);
                    stmt.setString(2, courseCode);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(deleteRegSql)) {
                    stmt.setString(1, courseCode);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(deleteRegCoursesSql)) {
                    stmt.setString(1, courseCode);
                    stmt.executeUpdate();
                }
                int affectedRows;
                try (PreparedStatement stmt = conn.prepareStatement(deleteCourseSql)) {
                    stmt.setString(1, courseCode);
                    affectedRows = stmt.executeUpdate();
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

    public static void updateCourse(Course course, String originalCourseCode) throws SQLException {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        String sql = """
                UPDATE courses
                SET course_code = ?, title = ?, credits = ?, instructor_name = ?, capacity = ?
                WHERE course_code = ?;
                """;
        String deleteSql = "DELETE FROM course_prereq WHERE course_code = ?;";
        String preReqSql = "INSERT OR IGNORE INTO course_prereq(course_code, prerequisite_id) VALUES(?, ?);";

        String targetCode = (originalCourseCode != null && !originalCourseCode.isBlank())
                ? originalCourseCode
                : course.getCourseCode();

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setString(1, course.getCourseCode());
                    preparedStatement.setString(2, course.getTitle());
                    preparedStatement.setInt(3, course.getCredits());
                    preparedStatement.setString(4, course.getInstructorName());
                    preparedStatement.setInt(5, course.getCapacity());
                    preparedStatement.setString(6, targetCode);
                    preparedStatement.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                    stmt.setString(1, targetCode);
                    stmt.executeUpdate();
                }

                String[] codes = course.getPrerequisiteCourses();
                if (codes != null && codes.length > 0) {
                    try (PreparedStatement query = conn.prepareStatement(preReqSql)) {
                        for (String code : codes) {
                            if (code != null && !code.isBlank()) {
                                query.setString(1, course.getCourseCode());
                                query.setString(2, code.trim().toUpperCase());
                                query.executeUpdate();
                            }
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

    public static Course[] searchCourse(String query, int limit, int offset) {
        String sql = """
                SELECT course_code, title, credits, instructor_name, capacity
                FROM courses
                WHERE course_code LIKE ?
                   OR title LIKE ?
                ORDER BY course_code
                LIMIT ? OFFSET ?;
                """;
        try (Connection conn = Database.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            String pattern = "%" + (query != null ? query.trim() : "") + "%";
            preparedStatement.setString(1, pattern);
            preparedStatement.setString(2, pattern);
            preparedStatement.setInt(3, limit);
            preparedStatement.setInt(4, offset);

            try (ResultSet result = preparedStatement.executeQuery()) {
                List<Course> courses = new ArrayList<>();
                while (result.next()) {
                    String code = result.getString("course_code");
                    int capacity = result.getInt("capacity");
                    if (capacity <= 0)
                        capacity = 30;
                    List<String> prereqs = getPrerequisites(code);
                    int enrolled = getEnrolledCount(code);

                    Course course = new Course(
                            code,
                            result.getString("title"),
                            result.getInt("credits"),
                            result.getString("instructor_name"),
                            prereqs,
                            capacity,
                            enrolled);
                    courses.add(course);
                }
                return courses.toArray(new Course[0]);
            }
        } catch (Exception e) {
            System.err.println("CourseDao.searchCourse error: " + e.getMessage());
            return new Course[0];
        }
    }

    public static List<Course> searchAllCourses(String query) {
        String sql = """
                SELECT course_code, title, credits, instructor_name, capacity
                FROM courses
                WHERE course_code LIKE ?
                   OR title LIKE ?
                ORDER BY course_code;
                """;
        List<Course> courses = new ArrayList<>();
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + (query != null ? query.trim() : "") + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("course_code");
                    int capacity = rs.getInt("capacity");
                    if (capacity <= 0)
                        capacity = 30;
                    List<String> prereqs = getPrerequisites(code);
                    int enrolled = getEnrolledCount(code);

                    Course course = new Course(
                            code,
                            rs.getString("title"),
                            rs.getInt("credits"),
                            rs.getString("instructor_name"),
                            prereqs,
                            capacity,
                            enrolled);
                    courses.add(course);
                }
            }
        } catch (Exception e) {
            System.err.println("CourseDao.searchAllCourses error: " + e.getMessage());
        }
        return courses;
    }

    public static Course getCourseByCode(String courseCode) {
        if (courseCode == null || courseCode.isBlank())
            return null;
        String sql = "SELECT course_code, title, credits, instructor_name, capacity FROM courses WHERE course_code = ?;";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String code = rs.getString("course_code");
                    int capacity = rs.getInt("capacity");
                    if (capacity <= 0)
                        capacity = 30;
                    List<String> prereqs = getPrerequisites(code);
                    int enrolled = getEnrolledCount(code);

                    return new Course(
                            code,
                            rs.getString("title"),
                            rs.getInt("credits"),
                            rs.getString("instructor_name"),
                            prereqs,
                            capacity,
                            enrolled);
                }
            }
        } catch (Exception e) {
            System.err.println("CourseDao.getCourseByCode error: " + e.getMessage());
        }
        return null;
    }

    public static List<String> getPrerequisites(String courseCode) {
        List<String> prereqs = new ArrayList<>();
        if (courseCode == null || courseCode.isBlank())
            return prereqs;
        String sql = "SELECT prerequisite_id FROM course_prereq WHERE course_code = ?;";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prereqs.add(rs.getString("prerequisite_id"));
                }
            }
        } catch (Exception e) {
            System.err.println("CourseDao.getPrerequisites error: " + e.getMessage());
        }
        return prereqs;
    }

    public static int getEnrolledCount(String courseCode) {
        if (courseCode == null || courseCode.isBlank())
            return 0;
        String sql = "SELECT COUNT(*) FROM registrations WHERE course_code = ? AND status = 'ACTIVE';";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            // fallback: check reg_courses
            try (Connection conn = Database.getConnection();
                    PreparedStatement stmt = conn
                            .prepareStatement("SELECT COUNT(*) FROM reg_courses WHERE course_code = ?;")) {
                stmt.setString(1, courseCode.trim().toUpperCase());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return rs.getInt(1);
                }
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    public static int getTotalCourseCount() {
        String sql = "SELECT COUNT(*) FROM courses;";
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
