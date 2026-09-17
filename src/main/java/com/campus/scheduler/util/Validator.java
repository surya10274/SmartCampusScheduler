package com.campus.scheduler.util;

import com.campus.scheduler.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Centralised input-validation rules used by the service layer.
 * Keeping validation in one place avoids duplicated, inconsistent checks
 * scattered across the codebase.
 */
public final class Validator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Validator() {
    }

    public static void requireNonBlank(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " must not be empty.");
        }
    }

    public static void requirePositive(int value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be a positive number.");
        }
    }

    public static void requireValidEmail(String email) throws ValidationException {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Email address is not valid: " + email);
        }
    }

    public static void requireValidTimeWindow(LocalDateTime start, LocalDateTime end) throws ValidationException {
        if (start == null || end == null) {
            throw new ValidationException("Start and end time are required.");
        }
        if (!end.isAfter(start)) {
            throw new ValidationException("End time must be after start time.");
        }
        if (start.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new ValidationException("Start time cannot be in the past.");
        }
    }
}
