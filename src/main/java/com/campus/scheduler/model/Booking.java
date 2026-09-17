package com.campus.scheduler.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a direct, ad-hoc booking of a resource by a user (distinct from a
 * full Event - e.g. a faculty member reserving a lab for an extra practice session).
 */
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public enum BookingStatus {
        CONFIRMED, CANCELLED
    }

    private final String bookingId;
    private final String resourceId;
    private final String userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private BookingStatus status;

    public Booking(String bookingId, String resourceId, String userId,
                    LocalDateTime startTime, LocalDateTime endTime, String purpose) {
        this.bookingId = bookingId;
        this.resourceId = resourceId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.status = BookingStatus.CONFIRMED;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public boolean overlapsWith(Booking other) {
        if (!this.resourceId.equals(other.resourceId)) {
            return false;
        }
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    public String toRecord() {
        return String.join("|", bookingId, resourceId, userId,
                startTime.format(FMT), endTime.format(FMT), purpose, status.name());
    }

    public static Booking fromRecord(String record) {
        String[] parts = record.split("\\|");
        Booking b = new Booking(parts[0], parts[1], parts[2],
                LocalDateTime.parse(parts[3], FMT), LocalDateTime.parse(parts[4], FMT), parts[5]);
        b.setStatus(BookingStatus.valueOf(parts[6]));
        return b;
    }

    @Override
    public String toString() {
        return String.format("[%s] Resource:%-8s User:%-8s %s -> %s Purpose:%-15s Status:%s",
                bookingId, resourceId, userId, startTime.format(FMT), endTime.format(FMT), purpose, status);
    }
}
