package com.courser.services;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.courser.dao.CourseDao;
import com.courser.dao.RegistrationDao;
import com.courser.dao.UserDao;
import com.courser.exception.CourseFullException;
import com.courser.exception.CreditLimitExceededException;
import com.courser.exception.DuplicateRegistrationException;
import com.courser.exception.PrerequisiteNotMetException;
import com.courser.exception.RegistrationException;
import com.courser.model.Course;
import com.courser.model.Registration;
import com.courser.model.Student;

/**
 * Service orchestrating course registration business rules:
 * 1. Enforcing course capacity limits.
 * 2. Verifying prerequisite completion.
 * 3. Preventing student semester credit limit overflow.
 * 4. Preventing duplicate course registrations.
 * 5. Managing course dropping and registration queries.
 */
public class RegistrationService {

    public static Registration registerCourse(String studentId, String courseCode) throws RegistrationException {
        if (studentId == null || studentId.isBlank()) {
            throw new RegistrationException("Student ID cannot be blank.");
        }
        if (courseCode == null || courseCode.isBlank()) {
            throw new RegistrationException("Course code cannot be blank.");
        }

        String sId = studentId.trim();
        String cCode = courseCode.trim().toUpperCase();

        Student student = UserDao.getStudentByStudentId(sId);
        if (student == null) {
            throw new RegistrationException("Student with ID '" + sId + "' was not found.");
        }

        Course course = CourseDao.getCourseByCode(cCode);
        if (course == null) {
            throw new RegistrationException("Course with code '" + cCode + "' was not found.");
        }

        // Rule 1: Check if already registered
        if (student.isRegisteredFor(cCode) || RegistrationDao.isStudentRegistered(sId, cCode)) {
            throw new DuplicateRegistrationException(sId, cCode);
        }

        // Rule 2: Check course capacity
        if (course.isFull()) {
            throw new CourseFullException(cCode, course.getCapacity());
        }

        // Rule 3: Verify prerequisite completion
        if (!student.hasCompletedPrerequisites(course)) {
            throw new PrerequisiteNotMetException(cCode, student.getMissingPrerequisites(course));
        }

        // Rule 4: Check credit limit
        int currentCredits = student.getRegisteredCredits();
        if (currentCredits + course.getCredits() > student.getMaxCredits()) {
            throw new CreditLimitExceededException(currentCredits, course.getCredits(), student.getMaxCredits());
        }

        // Apply domain updates
        boolean enrolled = course.enroll();
        if (!enrolled) {
            throw new CourseFullException(cCode, course.getCapacity());
        }
        student.registerCourse(course);

        // Record official registration
        Registration registration = new Registration(0, student, course, LocalDate.now(), Registration.STATUS_ACTIVE);
        try {
            RegistrationDao.addRegistration(registration);
        } catch (SQLException e) {
            // Rollback in-memory state
            course.drop();
            student.dropCourse(course);
            throw new RegistrationException("Failed to save registration: " + e.getMessage(), e);
        }

        return registration;
    }

    public static void persistRegistration(Registration registration) throws SQLException {
        RegistrationDao.addRegistration(registration);
    }

    public static boolean dropCourse(String studentId, String courseCode) {
        return dropRegistration(studentId, courseCode);
    }

    public static boolean dropRegistration(String studentId, String courseCode) {
        if (studentId == null || courseCode == null) return false;
        try {
            boolean dropped = RegistrationDao.dropRegistration(studentId.trim(), courseCode.trim().toUpperCase());
            if (dropped) {
                Course course = CourseDao.getCourseByCode(courseCode.trim().toUpperCase());
                if (course != null) {
                    course.drop();
                }
                Student student = UserDao.getStudentByStudentId(studentId.trim());
                if (student != null) {
                    student.dropCourse(courseCode.trim().toUpperCase());
                }
            }
            return dropped;
        } catch (SQLException e) {
            System.err.println("Error dropping registration: " + e.getMessage());
            return false;
        }
    }

    public static List<Registration> getStudentRegistrations(String studentId) {
        return RegistrationDao.getRegistrationsForStudent(studentId);
    }

    public static List<Course> getRegisteredCoursesForStudent(String studentId) {
        return RegistrationDao.getRegisteredCoursesForStudent(studentId);
    }

    public static List<Registration> getCourseRegistrations(String courseCode) {
        return RegistrationDao.getRegistrationsForCourse(courseCode);
    }

    public static List<Registration> getAllRegistrations() {
        return RegistrationDao.getAllRegistrations();
    }

    public static List<Registration> searchRegistrations(String query, int limit, int offset) {
        return RegistrationDao.searchRegistrations(query, limit, offset);
    }

    public static int getTotalRegistrationsCount(String query) {
        return RegistrationDao.getRegistrationCount(query);
    }
}
