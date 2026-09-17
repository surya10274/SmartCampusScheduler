package com.campus.scheduler.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a campus event (seminar, workshop, cultural fest, club meeting, etc.)
 * that requires one booked resource for a fixed time window.
 */
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public enum EventStatus {
        SCHEDULED, CANCELLED, COMPLETED
    }

    private final String eventId;
    private String title;
    private String organizerId;   // references User.userId
    private String resourceId;    // references Resource.resourceId
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int expectedAttendance;
    private EventStatus status;

    public Event(String eventId, String title, String organizerId, String resourceId,
                 LocalDateTime startTime, LocalDateTime endTime, int expectedAttendance) {
        this.eventId = eventId;
        this.title = title;
        this.organizerId = organizerId;
        this.resourceId = resourceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.expectedAttendance = expectedAttendance;
        this.status = EventStatus.SCHEDULED;
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getExpectedAttendance() {
        return expectedAttendance;
    }

    public void setExpectedAttendance(int expectedAttendance) {
        this.expectedAttendance = expectedAttendance;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    /**
     * Two events overlap if they use the same resource and their time windows intersect.
     */
    public boolean overlapsWith(Event other) {
        if (!this.resourceId.equals(other.resourceId)) {
            return false;
        }
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    public String toRecord() {
        return String.join("|", eventId, title, organizerId, resourceId,
                startTime.format(FMT), endTime.format(FMT),
                String.valueOf(expectedAttendance), status.name());
    }

    public static Event fromRecord(String record) {
        String[] parts = record.split("\\|");
        Event e = new Event(parts[0], parts[1], parts[2], parts[3],
                LocalDateTime.parse(parts[4], FMT), LocalDateTime.parse(parts[5], FMT),
                Integer.parseInt(parts[6]));
        e.setStatus(EventStatus.valueOf(parts[7]));
        return e;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-25s Resource:%-8s Organizer:%-8s %s -> %s Attendance:%-5d Status:%s",
                eventId, title, resourceId, organizerId,
                startTime.format(FMT), endTime.format(FMT), expectedAttendance, status);
    }
}
