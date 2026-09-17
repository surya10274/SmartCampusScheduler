package com.campus.scheduler.model;

import java.io.Serializable;

/**
 * Represents a bookable campus resource such as a room, lab, or equipment.
 * This is one of the core domain entities of the Smart Campus Resource and
 * Event Scheduler application.
 */
public class Resource implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum ResourceType {
        CLASSROOM, LAB, AUDITORIUM, SPORTS_FACILITY, EQUIPMENT, MEETING_ROOM
    }

    public enum ResourceStatus {
        AVAILABLE, UNDER_MAINTENANCE, DECOMMISSIONED
    }

    private final String resourceId;
    private String name;
    private ResourceType type;
    private int capacity;
    private String location;
    private ResourceStatus status;

    public Resource(String resourceId, String name, ResourceType type, int capacity, String location) {
        this.resourceId = resourceId;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.location = location;
        this.status = ResourceStatus.AVAILABLE;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    /**
     * Serializes this resource to a pipe-delimited record for flat-file storage.
     */
    public String toRecord() {
        return String.join("|", resourceId, name, type.name(), String.valueOf(capacity), location, status.name());
    }

    public static Resource fromRecord(String record) {
        String[] parts = record.split("\\|");
        Resource r = new Resource(parts[0], parts[1], ResourceType.valueOf(parts[2]), Integer.parseInt(parts[3]), parts[4]);
        r.setStatus(ResourceStatus.valueOf(parts[5]));
        return r;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s Type:%-15s Capacity:%-5d Location:%-15s Status:%s",
                resourceId, name, type, capacity, location, status);
    }
}
