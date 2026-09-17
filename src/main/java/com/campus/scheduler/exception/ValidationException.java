package com.campus.scheduler.exception;

/**
 * Thrown when user-supplied input fails validation rules
 * (e.g. empty name, non-positive capacity, end time before start time).
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
