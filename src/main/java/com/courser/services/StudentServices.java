package com.courser.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import com.courser.dao.RegistrationDao;
import com.courser.dao.UserDao;
import com.courser.model.Course;
import com.courser.model.Student;

public class StudentServices {

    public static void registerNewStudent(Student student, String[] courseCodes) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        if (student.getStudentId() == null || student.getStudentId().isBlank()) {
            throw new IllegalArgumentException("Student ID cannot be blank.");
        }
        if (student.getName() == null || student.getName().isBlank()) {
            throw new IllegalArgumentException("Student name cannot be blank.");
        }
        if (student.getMaxCredits() <= 0) {
            throw new IllegalArgumentException("Max credits must be positive.");
        }
        try {
            UserDao.addStudent(student);
            if (courseCodes != null) {
                for (String code : courseCodes) {
                    if (code != null && !code.isBlank()) {
                        try {
                            RegistrationService.registerCourse(student.getStudentId(), code.trim());
                        } catch (Exception e) {
                            System.err.println("Could not auto-register for course " + code + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error registering new student: " + e.getMessage());
            throw new RuntimeException("Failed to register student: " + e.getMessage(), e);
        }
    }

    public static Student[] searchStudents(String query, int limit, int offset) {
        try {
            return UserDao.searchStudent(query != null ? query : "", limit, offset);
        } catch (Exception e) {
            System.err.println("Error searching students: " + e.getMessage());
            return new Student[0];
        }
    }

    public static List<Student> searchAllStudents(String query) {
        return UserDao.searchAllStudents(query != null ? query : "");
    }

    public static List<Student> getAllStudents() {
        return UserDao.searchAllStudents("");
    }

    public static Student getStudentFormUserId(int id) {
        return getStudentFromUserId(id);
    }

    public static Student getStudentFromUserId(int id) {
        try {
            return UserDao.getStudentFromUserId(id);
        } catch (Exception e) {
            System.err.println("Error getting student from user ID: " + e.getMessage());
            return null;
        }
    }

    public static Student getStudentByStudentId(String studentId) {
        return UserDao.getStudentByStudentId(studentId);
    }

    public static void editStudentDetails(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        try {
            UserDao.updateStudent(student);
        } catch (Exception e) {
            System.err.println("Error updating student: " + e.getMessage());
            throw new RuntimeException("Failed to update student: " + e.getMessage(), e);
        }
    }

    public static boolean removeStudent(String studentId) {
        try {
            return UserDao.removeStudentByStudentId(studentId);
        } catch (Exception e) {
            System.err.println("Error removing student: " + e.getMessage());
            return false;
        }
    }

    public static boolean removeStudentByUserId(int userId) {
        try {
            return UserDao.removeStudent(userId);
        } catch (Exception e) {
            System.err.println("Error removing student: " + e.getMessage());
            return false;
        }
    }

    public static void addCompletedCourse(String studentId, String courseCode) {
        try {
            UserDao.addCompletedCourse(studentId, courseCode);
        } catch (SQLException e) {
            System.err.println("Error adding completed course: " + e.getMessage());
        }
    }

    public static void removeCompletedCourse(String studentId, String courseCode) {
        try {
            UserDao.removeCompletedCourse(studentId, courseCode);
        } catch (SQLException e) {
            System.err.println("Error removing completed course: " + e.getMessage());
        }
    }

    public static Set<String> getCompletedCourses(String studentId) {
        return UserDao.getCompletedCourseCodes(studentId);
    }

    public static List<Course> getRegisteredCoursesForStudent(String studentId) {
        return RegistrationDao.getRegisteredCoursesForStudent(studentId);
    }

    public static int getTotalCount() {
        return UserDao.getTotalStudentCount();
    }
}
