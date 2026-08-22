package com.courser.exception;

/**
 * Thrown when attempting to register for a course that has reached its maximum enrollment capacity.
 */
public class CourseFullException extends RegistrationException {
    private final String courseCode;
    private final int capacity;

    public CourseFullException(String courseCode, int capacity) {
        super("Cannot register for course '" + courseCode + "': Course is full (Capacity limit of " + capacity + " reached).");
        this.courseCode = courseCode;
        this.capacity = capacity;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public int getCapacity() {
        return capacity;
    }
}
