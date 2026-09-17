package com.campus.scheduler.service;

import com.campus.scheduler.model.Event;
import com.campus.scheduler.model.Resource;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Module 3 (part B): Reporting & Analytics.
 * Computes aggregate statistics over resources and events - the kind of
 * summary a campus facilities office would want: utilisation rate per
 * resource, busiest resources, and event load per organizer.
 */
public class ReportService {

    private final ResourceService resourceService;
    private final EventSchedulerService eventSchedulerService;

    public ReportService(ResourceService resourceService, EventSchedulerService eventSchedulerService) {
        this.resourceService = resourceService;
        this.eventSchedulerService = eventSchedulerService;
    }

    /** Returns total booked hours per resource across all non-cancelled events. */
    public Map<String, Double> getUtilizationHoursByResource() {
        Map<String, Double> utilization = new LinkedHashMap<>();
        for (Resource r : resourceService.getAll()) {
            utilization.put(r.getResourceId(), 0.0);
        }
        for (Event e : eventSchedulerService.getAll()) {
            if (e.getStatus() == Event.EventStatus.CANCELLED) {
                continue;
            }
            double hours = Duration.between(e.getStartTime(), e.getEndTime()).toMinutes() / 60.0;
            utilization.merge(e.getResourceId(), hours, Double::sum);
        }
        return utilization;
    }

    /** Ranks resources by number of active events, descending - identifies the "busiest" resources. */
    public List<Map.Entry<String, Long>> getBusiestResources(int topN) {
        Map<String, Long> counts = eventSchedulerService.getAll().stream()
                .filter(e -> e.getStatus() != Event.EventStatus.CANCELLED)
                .collect(Collectors.groupingBy(Event::getResourceId, Collectors.counting()));
        return counts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /** Number of active (non-cancelled) events organised by each user. */
    public Map<String, Long> getEventLoadByOrganizer() {
        return eventSchedulerService.getAll().stream()
                .filter(e -> e.getStatus() != Event.EventStatus.CANCELLED)
                .collect(Collectors.groupingBy(Event::getOrganizerId, Collectors.counting()));
    }

    /** Simple counts breakdown - useful for a quick dashboard summary. */
    public Map<String, Long> getEventStatusBreakdown() {
        return eventSchedulerService.getAll().stream()
                .collect(Collectors.groupingBy(e -> e.getStatus().name(), Collectors.counting()));
    }

    public double getAverageAttendance() {
        List<Event> active = eventSchedulerService.getAll().stream()
                .filter(e -> e.getStatus() != Event.EventStatus.CANCELLED)
                .collect(Collectors.toList());
        if (active.isEmpty()) {
            return 0.0;
        }
        return active.stream().mapToInt(Event::getExpectedAttendance).average().orElse(0.0);
    }
}
