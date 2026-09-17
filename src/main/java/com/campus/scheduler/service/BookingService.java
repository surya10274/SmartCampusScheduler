package com.campus.scheduler.service;

import com.campus.scheduler.exception.EntityNotFoundException;
import com.campus.scheduler.exception.ScheduleConflictException;
import com.campus.scheduler.exception.ValidationException;
import com.campus.scheduler.model.Booking;
import com.campus.scheduler.model.Event;
import com.campus.scheduler.model.Resource;
import com.campus.scheduler.util.FileStorageUtil;
import com.campus.scheduler.util.IdGenerator;
import com.campus.scheduler.util.Logger;
import com.campus.scheduler.util.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 3 (part A): Ad-hoc Resource Booking.
 * Distinct from full Events - this lets any registered user reserve a
 * resource directly (e.g. a quick study-room booking) while still sharing
 * the same conflict-free guarantee, checked against both existing bookings
 * AND scheduled events on that resource.
 */
public class BookingService {

    private static final String FILE_NAME = "bookings.txt";
    private final List<Booking> bookings;
    private final ResourceService resourceService;
    private final EventSchedulerService eventSchedulerService;

    public BookingService(ResourceService resourceService, EventSchedulerService eventSchedulerService) {
        this.resourceService = resourceService;
        this.eventSchedulerService = eventSchedulerService;
        this.bookings = FileStorageUtil.loadAll(FILE_NAME, Booking::fromRecord);
        int highest = bookings.stream()
                .map(b -> b.getBookingId().replaceAll("[^0-9]", ""))
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .max().orElse(0);
        IdGenerator.primeBookingCounter(highest);
        Logger.info("BookingService initialised with " + bookings.size() + " booking(s).");
    }

    public Booking createBooking(String resourceId, String userId, LocalDateTime start, LocalDateTime end, String purpose)
            throws ValidationException, EntityNotFoundException, ScheduleConflictException {

        Validator.requireValidTimeWindow(start, end);
        Validator.requireNonBlank(purpose, "Purpose");
        Resource resource = resourceService.getById(resourceId);
        if (resource.getStatus() != Resource.ResourceStatus.AVAILABLE) {
            throw new ScheduleConflictException("Resource " + resourceId + " is not available for booking.");
        }

        Booking candidate = new Booking(IdGenerator.nextBookingId(), resourceId, userId, start, end, purpose);

        for (Booking existing : bookings) {
            if (existing.getStatus() == Booking.BookingStatus.CANCELLED) {
                continue;
            }
            if (existing.overlapsWith(candidate)) {
                throw new ScheduleConflictException("Booking conflict with existing booking " + existing.getBookingId()
                        + " on resource " + resourceId);
            }
        }
        for (Event event : eventSchedulerService.getEventsForResource(resourceId)) {
            if (event.getStatus() == Event.EventStatus.CANCELLED) {
                continue;
            }
            boolean overlaps = start.isBefore(event.getEndTime()) && event.getStartTime().isBefore(end);
            if (overlaps) {
                throw new ScheduleConflictException("Booking conflict with scheduled event " + event.getEventId()
                        + " on resource " + resourceId);
            }
        }

        bookings.add(candidate);
        persist();
        Logger.info("Created booking " + candidate.getBookingId() + " for resource " + resourceId);
        return candidate;
    }

    public void cancelBooking(String bookingId) throws EntityNotFoundException {
        Booking booking = getById(bookingId);
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        persist();
        Logger.info("Cancelled booking " + bookingId);
    }

    public Booking getById(String bookingId) throws EntityNotFoundException {
        return bookings.stream()
                .filter(b -> b.getBookingId().equalsIgnoreCase(bookingId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + bookingId));
    }

    public List<Booking> getAll() {
        return new ArrayList<>(bookings);
    }

    public List<Booking> getBookingsForUser(String userId) {
        return bookings.stream().filter(b -> b.getUserId().equalsIgnoreCase(userId)).collect(Collectors.toList());
    }

    private void persist() {
        FileStorageUtil.saveAll(FILE_NAME, bookings, Booking::toRecord);
    }
}
