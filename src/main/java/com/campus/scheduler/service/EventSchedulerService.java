package com.campus.scheduler.service;

import com.campus.scheduler.exception.EntityNotFoundException;
import com.campus.scheduler.exception.ScheduleConflictException;
import com.campus.scheduler.exception.ValidationException;
import com.campus.scheduler.model.Event;
import com.campus.scheduler.model.Resource;
import com.campus.scheduler.util.FileStorageUtil;
import com.campus.scheduler.util.IdGenerator;
import com.campus.scheduler.util.Logger;
import com.campus.scheduler.util.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 2: Event Scheduling.
 * This is the heart of the application - it books a Resource for an Event
 * while guaranteeing no two events ever double-book the same resource at
 * an overlapping time (the core "smart scheduling" logic).
 */
public class EventSchedulerService {

    private static final String FILE_NAME = "events.txt";
    private final List<Event> events;
    private final ResourceService resourceService;

    public EventSchedulerService(ResourceService resourceService) {
        this.resourceService = resourceService;
        this.events = FileStorageUtil.loadAll(FILE_NAME, Event::fromRecord);
        int highest = events.stream()
                .map(e -> e.getEventId().replaceAll("[^0-9]", ""))
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .max().orElse(0);
        IdGenerator.primeEventCounter(highest);
        Logger.info("EventSchedulerService initialised with " + events.size() + " event(s).");
    }

    public Event scheduleEvent(String title, String organizerId, String resourceId,
                                LocalDateTime start, LocalDateTime end, int expectedAttendance)
            throws ValidationException, EntityNotFoundException, ScheduleConflictException {

        Validator.requireNonBlank(title, "Event title");
        Validator.requireValidTimeWindow(start, end);
        Validator.requirePositive(expectedAttendance, "Expected attendance");

        Resource resource = resourceService.getById(resourceId);
        if (resource.getStatus() != Resource.ResourceStatus.AVAILABLE) {
            throw new ScheduleConflictException(
                    "Resource " + resourceId + " is not available (status: " + resource.getStatus() + ").");
        }
        if (expectedAttendance > resource.getCapacity()) {
            throw new ValidationException("Expected attendance (" + expectedAttendance
                    + ") exceeds resource capacity (" + resource.getCapacity() + ").");
        }

        Event candidate = new Event(IdGenerator.nextEventId(), title, organizerId, resourceId, start, end, expectedAttendance);
        checkConflict(candidate, null);

        events.add(candidate);
        persist();
        Logger.info("Scheduled event " + candidate.getEventId() + " (" + title + ") on resource " + resourceId);
        return candidate;
    }

    /**
     * Core conflict-detection algorithm: an O(n) scan against all active
     * (non-cancelled) events sharing the same resource. excludeEventId lets
     * reschedule operations skip comparing an event against itself.
     */
    private void checkConflict(Event candidate, String excludeEventId) throws ScheduleConflictException {
        for (Event existing : events) {
            if (existing.getStatus() == Event.EventStatus.CANCELLED) {
                continue;
            }
            if (excludeEventId != null && existing.getEventId().equals(excludeEventId)) {
                continue;
            }
            if (existing.overlapsWith(candidate)) {
                throw new ScheduleConflictException(
                        "Time conflict with existing event " + existing.getEventId()
                                + " (" + existing.getTitle() + ") on resource " + candidate.getResourceId()
                                + " between " + existing.getStartTime() + " and " + existing.getEndTime());
            }
        }
    }

    public Event rescheduleEvent(String eventId, LocalDateTime newStart, LocalDateTime newEnd)
            throws EntityNotFoundException, ValidationException, ScheduleConflictException {
        Event event = getById(eventId);
        Validator.requireValidTimeWindow(newStart, newEnd);
        Event candidate = new Event(event.getEventId(), event.getTitle(), event.getOrganizerId(),
                event.getResourceId(), newStart, newEnd, event.getExpectedAttendance());
        checkConflict(candidate, eventId);
        event.setStartTime(newStart);
        event.setEndTime(newEnd);
        persist();
        Logger.info("Rescheduled event " + eventId);
        return event;
    }

    public void cancelEvent(String eventId) throws EntityNotFoundException {
        Event event = getById(eventId);
        event.setStatus(Event.EventStatus.CANCELLED);
        persist();
        Logger.info("Cancelled event " + eventId);
    }

    public void markCompleted(String eventId) throws EntityNotFoundException {
        Event event = getById(eventId);
        event.setStatus(Event.EventStatus.COMPLETED);
        persist();
    }

    public Event getById(String eventId) throws EntityNotFoundException {
        return events.stream()
                .filter(e -> e.getEventId().equalsIgnoreCase(eventId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + eventId));
    }

    public List<Event> getAll() {
        return new ArrayList<>(events);
    }

    public List<Event> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        return events.stream()
                .filter(e -> e.getStatus() == Event.EventStatus.SCHEDULED && e.getStartTime().isAfter(now))
                .sorted(Comparator.comparing(Event::getStartTime))
                .collect(Collectors.toList());
    }

    public List<Event> getEventsForResource(String resourceId) {
        return events.stream()
                .filter(e -> e.getResourceId().equalsIgnoreCase(resourceId))
                .sorted(Comparator.comparing(Event::getStartTime))
                .collect(Collectors.toList());
    }

    public List<Event> getEventsForOrganizer(String organizerId) {
        return events.stream()
                .filter(e -> e.getOrganizerId().equalsIgnoreCase(organizerId))
                .sorted(Comparator.comparing(Event::getStartTime))
                .collect(Collectors.toList());
    }

    private void persist() {
        FileStorageUtil.saveAll(FILE_NAME, events, Event::toRecord);
    }
}
