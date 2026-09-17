package com.campus.scheduler.exception;

/**
 * Thrown when a new Event or Booking would overlap with an existing one
 * on the same resource.
 */
public class ScheduleConflictException extends Exception {

    public ScheduleConflictException(String message) {
        super(message);
    }
}
