package com.courser.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.courser.exception.CourseFullException;
import com.courser.exception.CreditLimitExceededException;
import com.courser.exception.DuplicateRegistrationException;
import com.courser.exception.PrerequisiteNotMetException;
import com.courser.exception.RegistrationException;
import com.courser.services.CourseServices;
import com.courser.services.RegistrationService;
import com.courser.services.StudentServices;

/**
 * Represents a Registrar in the Course Management System.
 * Inherits from User and possesses elevated capabilities to manage courses,
 * register/manage students, and oversee course registrations.
 */
public class Registrar extends User {
    private String registrarId;
    private String department;

    public Registrar(int userId, String userName, String registrarId, String name, String email, String department) {
        super(userId, userName, name, email);
        setRegistrarId(registrarId);
        setDepartment(department);
    }

    public Registrar(String userName, String registrarId, String name, String email, String department) {
        this(0, userName, registrarId, name, email, department);
    }

    public String getRegistrarId() {
        return registrarId;
    }

    public void setRegistrarId(String registrarId) {
        this.registrarId = Objects.requireNonNull(registrarId, "Registrar ID cannot be null").trim();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department != null ? department.trim() : "Office of the Registrar";
    }

    // ==========================================
    // Course Management Capabilities
    // ==========================================

    /**
     * Add a new course to the catalog.
     */
    public void addCourse(Course course) {
        CourseServices.addNewCourse(course);
    }

    /**
     * Remove an existing course by course code.
     */
    public boolean removeCourse(String courseCode) {
        return CourseServices.removeCourse(courseCode);
    }

    /**
     * Update course details.
     */
    public void updateCourse(Course course, String originalCourseCode) {
        CourseServices.updateCourse(course, originalCourseCode);
    }

    /**
     * Search courses by course code or title.
     */
    public List<Course> searchCourses(String query) {
        return CourseServices.searchAllCourses(query);
    }

    // ==========================================
    // Student Management Capabilities
    // ==========================================

    /**
     * Register a new student in the system.
     */
    public void registerStudent(Student student) {
        StudentServices.registerNewStudent(student, null);
    }

    /**
     * Search students by student ID or name.
     */
    public List<Student> searchStudents(String query) {
        return StudentServices.searchAllStudents(query);
    }

    /**
     * Retrieve a student by student ID.
     */
    public Student getStudent(String studentId) {
        return StudentServices.getStudentByStudentId(studentId);
    }

    // ==========================================
    // Course Registration Capabilities
    // ==========================================

    /**
     * Register a student for a course, enforcing prerequisite, credit-limit, and capacity rules.
     */
    public Registration registerStudentForCourse(Student student, Course course) throws RegistrationException {
        if (student == null) {
            throw new RegistrationException("Student cannot be null.");
        }
        if (course == null) {
            throw new RegistrationException("Course cannot be null.");
        }

        // 1. Check duplicate registration
        if (student.isRegisteredFor(course.getCourseCode())) {
            throw new DuplicateRegistrationException(student.getStudentId(), course.getCourseCode());
        }

        // 2. Check course capacity
        if (course.isFull()) {
            throw new CourseFullException(course.getCourseCode(), course.getCapacity());
        }

        // 3. Verify prerequisites
        if (!student.hasCompletedPrerequisites(course)) {
            throw new PrerequisiteNotMetException(course.getCourseCode(), student.getMissingPrerequisites(course));
        }

        // 4. Check credit limit
        int currentCredits = student.getRegisteredCredits();
        if (currentCredits + course.getCredits() > student.getMaxCredits()) {
            throw new CreditLimitExceededException(currentCredits, course.getCredits(), student.getMaxCredits());
        }

        // Apply domain updates
        boolean enrolled = course.enroll();
        if (!enrolled) {
            throw new CourseFullException(course.getCourseCode(), course.getCapacity());
        }
        student.registerCourse(course);

        // Record official registration
        Registration registration = new Registration(0, student, course, LocalDate.now(), Registration.STATUS_ACTIVE);
        try {
            RegistrationService.persistRegistration(registration);
        } catch (Exception e) {
            // Rollback in-memory state on db error
            course.drop();
            student.dropCourse(course);
            throw new RegistrationException("Failed to persist registration: " + e.getMessage(), e);
        }
        return registration;
    }

    /**
     * Drop a course for a student.
     */
    public boolean dropStudentFromCourse(Student student, Course course) {
        if (student == null || course == null) return false;
        boolean droppedStudent = student.dropCourse(course);
        if (droppedStudent) {
            course.drop();
            RegistrationService.dropRegistration(student.getStudentId(), course.getCourseCode());
            return true;
        }
        return false;
    }

    /**
     * View all active registrations in the system.
     */
    public List<Registration> viewAllRegistrations() {
        return RegistrationService.getAllRegistrations();
    }

    /**
     * View registrations for a specific student.
     */
    public List<Registration> viewStudentRegistrations(String studentId) {
        return RegistrationService.getStudentRegistrations(studentId);
    }

    /**
     * View the list of courses a student is registered for.
     */
    public List<Course> viewStudentCourses(String studentId) {
        return RegistrationService.getRegisteredCoursesForStudent(studentId);
    }

    /**
     * View registrations for a specific course.
     */
    public List<Registration> viewCourseRegistrations(String courseCode) {
        return RegistrationService.getCourseRegistrations(courseCode);
    }

    @Override
    public String toString() {
        return "Registrar{" +
                "registrarId='" + registrarId + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
