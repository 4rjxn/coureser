package com.courser.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Vector;

import com.courser.model.Course;
import com.courser.utils.Database;

public class CourseDao {
    public static boolean addNew(Course course) {
        String sql = """
                INSERT INTO courses(course_code,title,credits,instructor_name)
                VALUES(?,?,?,?)
                """;
        try {
            Connection conn = Database.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, course.getCourseCode());
            preparedStatement.setString(2, course.getTitle());
            preparedStatement.setString(3, String.valueOf(course.getCredits()));
            preparedStatement.setString(4, course.getInstructorName());
            preparedStatement.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println(e);
            return false;
        }

    }

    boolean removeCourse() {
        // TODO: remove courese;
        throw new UnsupportedOperationException("Not implemented yet.");

    }

    public static boolean updateCourse(Course course, String course_code) {
        String sql = """
                    UPDATE courses
                    SET course_code = ?, title = ?, credits = ?, instructor_name = ?
                    WHERE course_code = ?
                """;
        try {
            Connection conn = Database.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, course.getCourseCode());
            preparedStatement.setString(2, course.getTitle());
            preparedStatement.setString(3, String.valueOf(course.getCredits()));
            preparedStatement.setString(4, course.getInstructorName());
            preparedStatement.setString(5, course_code);
            preparedStatement.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println(e);
            return false;
        }

    }

    public static Course[] getCourses(int limit, int offset) {
        String sql = """
                    SELECT *
                    FROM courses
                    ORDER BY course_code
                    LIMIT ? OFFSET ?;
                """;
        try {
            Connection conn = Database.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, String.valueOf(limit));
            preparedStatement.setString(2, String.valueOf(offset));
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
