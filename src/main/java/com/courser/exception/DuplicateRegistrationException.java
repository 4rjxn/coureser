package com.courser.exception;

/**
 * Thrown when a student attempts to register for a course they are already actively registered in.
 */
public class DuplicateRegistrationException extends RegistrationException {
    private final String studentId;
    private final String courseCode;

    public DuplicateRegistrationException(String studentId, String courseCode) {
        super("Cannot register for course '" + courseCode + "': Student '" + studentId + "' is already registered for this course.");
        this.studentId = studentId;
        this.courseCode = courseCode;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCourseCode() {
        return courseCode;
    }
}
