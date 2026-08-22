package com.courser.services;

import java.sql.SQLException;
import java.util.List;

import com.courser.dao.CourseDao;
import com.courser.model.Course;

public class CourseServices {

    public static void addNewCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        if (course.getCourseCode() == null || course.getCourseCode().isBlank()) {
            throw new IllegalArgumentException("Course code cannot be blank.");
        }
        if (course.getTitle() == null || course.getTitle().isBlank()) {
            throw new IllegalArgumentException("Course title cannot be blank.");
        }
        if (course.getCredits() <= 0) {
            throw new IllegalArgumentException("Course credits must be greater than 0.");
        }
        if (course.getCapacity() <= 0) {
            throw new IllegalArgumentException("Course capacity must be greater than 0.");
        }
        try {
            CourseDao.addNew(course);
        } catch (SQLException e) {
            System.err.println("Error adding new course: " + e.getMessage());
            throw new RuntimeException("Failed to add course: " + e.getMessage(), e);
        }
    }

    public static boolean removeCourse(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            return false;
        }
        try {
            return CourseDao.removeCourse(courseCode.trim().toUpperCase());
        } catch (SQLException e) {
            System.err.println("Error removing course: " + e.getMessage());
            return false;
        }
    }

    public static Course[] searchCourses(String query, int limit, int offset) {
        return CourseDao.searchCourse(query != null ? query : "", limit, offset);
    }

    public static List<Course> searchAllCourses(String query) {
        return CourseDao.searchAllCourses(query != null ? query : "");
    }

    public static List<Course> getAllCourses() {
        return CourseDao.searchAllCourses("");
    }

    public static Course getCourseByCode(String courseCode) {
        return CourseDao.getCourseByCode(courseCode);
    }

    public static void updateCourse(Course course, String originalCourseCode) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        try {
            CourseDao.updateCourse(course, originalCourseCode);
        } catch (SQLException e) {
            System.err.println("Error updating course: " + e.getMessage());
            throw new RuntimeException("Failed to update course: " + e.getMessage(), e);
        }
    }

    public static int getTotalCount() {
        return CourseDao.getTotalCourseCount();
    }
}
