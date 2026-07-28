package com.courser.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import com.courser.model.Course;
import com.courser.utils.Database;

public class CourseDao {
    public static void addNew(Course course) throws SQLException {
        String courseSql = """
                INSERT INTO courses(course_code,title,credits,instructor_name)
                VALUES(?,?,?,?)
                """;
        String preReqSql = """
                INSERT INTO course_prereq(course_code,prerequisite_id)
                VALUES(?,?)
                """;
        try (Connection conn = Database.getConnection()) {
            try {
                conn.setAutoCommit(false);
                PreparedStatement stmt = conn.prepareStatement(courseSql);
                stmt.setString(1, course.getCourseCode());
                stmt.setString(2, course.getTitle());
                stmt.setInt(3, course.getCredits());
                stmt.setString(4, course.getInstructorName());
                stmt.execute();
                stmt.close();

                String[] codes = course.getPrerequisiteCourses();
                if (codes != null) {
                    PreparedStatement query = conn.prepareStatement(preReqSql);
                    for (String code : codes) {
                        query.setString(1, course.getCourseCode());
                        query.setString(2, code);
                        query.execute();
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                System.out.println(e);
            } finally {
                conn.setAutoCommit(true);
            }
        }

    }

    boolean removeCourse() {
        // TODO: remove courese;
        throw new UnsupportedOperationException("Not implemented yet.");

    }

    public static void updateCourse(Course course, String course_code) throws SQLException {
        String sql = """
                    UPDATE courses
                    SET course_code = ?, title = ?, credits = ?, instructor_name = ?
                    WHERE course_code = ?;
                """;
        String deleteSql = """
                DELETE FROM course_prereq
                WHERE course_code = ?;
                    """;
        String preReqSql = """
                INSERT INTO course_prereq(course_code,prerequisite_id)
                VALUES(?,?)
                """;
        try (Connection conn = Database.getConnection()) {
            try {
                conn.setAutoCommit(false);
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, course.getCourseCode());
                preparedStatement.setString(2, course.getTitle());
                preparedStatement.setInt(3, course.getCredits());
                preparedStatement.setString(4, course.getInstructorName());
                preparedStatement.setString(5, course_code);
                preparedStatement.executeUpdate();
                preparedStatement.close();

                PreparedStatement stmt = conn.prepareStatement(deleteSql);
                stmt.setString(1, course.getCourseCode());
                stmt.execute();
                stmt.close();

                String[] codes = course.getPrerequisiteCourses();
                if (codes != null) {
                    PreparedStatement query = conn.prepareStatement(preReqSql);
                    for (String code : codes) {
                        query.setString(1, course.getCourseCode());
                        query.setString(2, code);
                        query.execute();
                    }
                }
                stmt.close();
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                System.out.println(e);
            } finally {
                conn.setAutoCommit(true);
            }
        }

    }

    public static Course[] searchCourse(String query, int limit, int offset) {
        String sql = """
                    SELECT *
                    FROM courses
                    WHERE course_code LIKE ?
                        OR title LIKE ?
                    ORDER BY course_code
                    LIMIT ? OFFSET ?;
                """;
        try {
            Connection conn = Database.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, query + "%");
            preparedStatement.setString(2, query + "%");
            preparedStatement.setString(3, String.valueOf(limit));
            preparedStatement.setString(4, String.valueOf(offset));
            ResultSet result = preparedStatement.executeQuery();

            List<Course> courses = new Vector<Course>();
            while (result.next()) {
                Course course = new Course(
                        result.getString("course_code"), result.getString("title"),
                        result.getInt("credits"),
                        result.getString("instructor_name"),
                        null);
                courses.add(course);
            }
            preparedStatement.close();
            return courses.toArray(new Course[0]);

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

    }

    Course[] search(int courseCode) {
        // TODO: search courese;
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    Course[] search(String courseName) {
        // TODO: search courese;
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
