package com.campus.scheduler.exception;

/**
 * Thrown when a lookup for a Resource, User, Event, or Booking by ID fails.
 */
public class EntityNotFoundException extends Exception {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
