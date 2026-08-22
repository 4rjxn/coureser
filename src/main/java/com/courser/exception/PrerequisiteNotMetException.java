package com.courser.exception;

import java.util.List;

/**
 * Thrown when a student attempts to register for a course without completing all prerequisites.
 */
public class PrerequisiteNotMetException extends RegistrationException {
    private final String courseCode;
    private final List<String> missingPrerequisites;

    public PrerequisiteNotMetException(String courseCode, List<String> missingPrerequisites) {
        super("Cannot register for course '" + courseCode + "': Missing required prerequisite(s): " + String.join(", ", missingPrerequisites));
        this.courseCode = courseCode;
        this.missingPrerequisites = missingPrerequisites;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public List<String> getMissingPrerequisites() {
        return missingPrerequisites;
    }
}
