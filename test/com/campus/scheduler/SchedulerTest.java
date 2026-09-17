package com.campus.scheduler;

import com.campus.scheduler.exception.EntityNotFoundException;
import com.campus.scheduler.exception.ScheduleConflictException;
import com.campus.scheduler.exception.ValidationException;
import com.campus.scheduler.model.Resource;
import com.campus.scheduler.model.User;
import com.campus.scheduler.service.BookingService;
import com.campus.scheduler.service.EventSchedulerService;
import com.campus.scheduler.service.ResourceService;
import com.campus.scheduler.service.UserService;

import java.time.LocalDateTime;

/**
 * Lightweight, dependency-free validation-test harness (no JUnit required
 * so the project stays buildable with `javac` alone). Run with:
 *   java -cp bin com.campus.scheduler.SchedulerTest
 *
 * Each check prints PASS/FAIL; a non-zero exit code is returned if any
 * check fails, which also makes this usable as a CI smoke test.
 */
public class SchedulerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("Running SmartCampusScheduler test suite...\n");

        testResourceValidation();
        testConflictDetectionRejectsOverlap();
        testConflictDetectionAllowsNonOverlap();
        testCapacityCheck();
        testBookingConflictsWithEvent();

        System.out.println("\n===== TEST SUMMARY =====");
        System.out.println("Passed: " + passed + "  Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void check(String label, boolean condition) {
        if (condition) {
            System.out.println("PASS - " + label);
            passed++;
        } else {
            System.out.println("FAIL - " + label);
            failed++;
        }
    }

    private static void testResourceValidation() {
        try {
            ResourceService rs = new ResourceService();
            boolean threw = false;
            try {
                rs.addResource("", Resource.ResourceType.LAB, 10, "X");
            } catch (ValidationException e) {
                threw = true;
            }
            check("Empty resource name is rejected", threw);
        } catch (Exception e) {
            check("testResourceValidation ran without crashing", false);
        }
    }

    private static void testConflictDetectionRejectsOverlap() {
        try {
            ResourceService rs = new ResourceService();
            EventSchedulerService es = new EventSchedulerService(rs);
            Resource r = rs.addResource("Test Room Overlap", Resource.ResourceType.CLASSROOM, 30, "Test Block");

            LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime end = start.plusHours(2);
            es.scheduleEvent("Workshop A", "U999", r.getResourceId(), start, end, 20);

            boolean conflictDetected = false;
            try {
                es.scheduleEvent("Workshop B", "U998", r.getResourceId(), start.plusMinutes(30), end.plusMinutes(30), 15);
            } catch (ScheduleConflictException e) {
                conflictDetected = true;
            }
            check("Overlapping event on same resource is rejected", conflictDetected);
        } catch (Exception e) {
            check("testConflictDetectionRejectsOverlap ran without crashing", false);
        }
    }

    private static void testConflictDetectionAllowsNonOverlap() {
        try {
            ResourceService rs = new ResourceService();
            EventSchedulerService es = new EventSchedulerService(rs);
            Resource r = rs.addResource("Test Room NonOverlap", Resource.ResourceType.CLASSROOM, 30, "Test Block");

            LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0);
            LocalDateTime end = start.plusHours(1);
            es.scheduleEvent("Session A", "U999", r.getResourceId(), start, end, 10);

            boolean noException = true;
            try {
                es.scheduleEvent("Session B", "U998", r.getResourceId(), end.plusMinutes(1), end.plusHours(1), 10);
            } catch (Exception e) {
                noException = false;
            }
            check("Back-to-back non-overlapping events are both accepted", noException);
        } catch (Exception e) {
            check("testConflictDetectionAllowsNonOverlap ran without crashing", false);
        }
    }

    private static void testCapacityCheck() {
        try {
            ResourceService rs = new ResourceService();
            EventSchedulerService es = new EventSchedulerService(rs);
            Resource r = rs.addResource("Small Room", Resource.ResourceType.CLASSROOM, 5, "Test Block");

            LocalDateTime start = LocalDateTime.now().plusDays(3).withHour(9).withMinute(0);
            LocalDateTime end = start.plusHours(1);

            boolean rejected = false;
            try {
                es.scheduleEvent("Overcapacity Event", "U999", r.getResourceId(), start, end, 50);
            } catch (ValidationException e) {
                rejected = true;
            }
            check("Event exceeding resource capacity is rejected", rejected);
        } catch (Exception e) {
            check("testCapacityCheck ran without crashing", false);
        }
    }

    private static void testBookingConflictsWithEvent() {
        try {
            ResourceService rs = new ResourceService();
            EventSchedulerService es = new EventSchedulerService(rs);
            BookingService bs = new BookingService(rs, es);
            Resource r = rs.addResource("Shared Lab", Resource.ResourceType.LAB, 20, "Test Block");

            LocalDateTime start = LocalDateTime.now().plusDays(4).withHour(14).withMinute(0);
            LocalDateTime end = start.plusHours(2);
            es.scheduleEvent("Lab Session", "U999", r.getResourceId(), start, end, 15);

            boolean conflictDetected = false;
            try {
                bs.createBooking(r.getResourceId(), "U998", start.plusMinutes(30), end.plusMinutes(30), "Extra practice");
            } catch (ScheduleConflictException e) {
                conflictDetected = true;
            }
            check("Direct booking that overlaps a scheduled event is rejected", conflictDetected);
        } catch (Exception e) {
            check("testBookingConflictsWithEvent ran without crashing", false);
        }
    }
}
