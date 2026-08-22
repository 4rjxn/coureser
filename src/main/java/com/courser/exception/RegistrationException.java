package com.courser.exception;

/**
 * Base exception thrown when a course registration operation fails.
 */
public class RegistrationException extends Exception {
    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
