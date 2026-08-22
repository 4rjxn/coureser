package com.courser.exception;

/**
 * Thrown when registering for a course would cause the student's total credits to exceed their allowed maximum.
 */
public class CreditLimitExceededException extends RegistrationException {
    private final int currentCredits;
    private final int courseCredits;
    private final int maxCredits;

    public CreditLimitExceededException(int currentCredits, int courseCredits, int maxCredits) {
        super("Cannot register for course: Total credits (" + (currentCredits + courseCredits) + 
              ") would exceed maximum allowed limit of " + maxCredits + " credits per semester (Current: " + 
              currentCredits + ", Course: " + courseCredits + ").");
        this.currentCredits = currentCredits;
        this.courseCredits = courseCredits;
        this.maxCredits = maxCredits;
    }

    public int getCurrentCredits() {
        return currentCredits;
    }

    public int getCourseCredits() {
        return courseCredits;
    }

    public int getMaxCredits() {
        return maxCredits;
    }
}
