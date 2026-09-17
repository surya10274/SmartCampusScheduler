package com.campus.scheduler.main;

import com.campus.scheduler.exception.EntityNotFoundException;
import com.campus.scheduler.exception.ScheduleConflictException;
import com.campus.scheduler.exception.ValidationException;
import com.campus.scheduler.model.Booking;
import com.campus.scheduler.model.Event;
import com.campus.scheduler.model.Resource;
import com.campus.scheduler.model.User;
import com.campus.scheduler.service.BookingService;
import com.campus.scheduler.service.EventSchedulerService;
import com.campus.scheduler.service.ReportService;
import com.campus.scheduler.service.ResourceService;
import com.campus.scheduler.service.UserService;
import com.campus.scheduler.util.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console entry point for the Smart Campus Resource and Event Scheduler.
 * Wires together the service layer and drives a menu-based workflow that
 * exercises all three functional modules: Resource Management, Event
 * Scheduling, and Reporting/Analytics.
 */
public class Main {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final Scanner scanner = new Scanner(System.in);

    private static final ResourceService resourceService = new ResourceService();
    private static final UserService userService = new UserService();
    private static final EventSchedulerService eventSchedulerService = new EventSchedulerService(resourceService);
    private static final BookingService bookingService = new BookingService(resourceService, eventSchedulerService);
    private static final ReportService reportService = new ReportService(resourceService, eventSchedulerService);

    public static void main(String[] args) {
        Logger.info("Smart Campus Resource and Event Scheduler starting up...");
        seedDemoDataIfEmpty();
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": resourceMenu(); break;
                    case "2": userMenu(); break;
                    case "3": eventMenu(); break;
                    case "4": bookingMenu(); break;
                    case "5": reportMenu(); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice, try again.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
                Logger.error("Unhandled error in menu loop: " + ex.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    // ---------------------------------------------------------------- MENUS

    private static void printMainMenu() {
        System.out.println("\n===== SMART CAMPUS RESOURCE AND EVENT SCHEDULER =====");
        System.out.println("1. Resource Management");
        System.out.println("2. User Management");
        System.out.println("3. Event Scheduling");
        System.out.println("4. Resource Booking");
        System.out.println("5. Reports & Analytics");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private static void resourceMenu() throws ValidationException, EntityNotFoundException {
        System.out.println("\n-- Resource Management --");
        System.out.println("1. Add resource | 2. List all | 3. Update resource | 4. Delete resource | 5. Search available | 0. Back");
        System.out.print("Choice: ");
        switch (scanner.nextLine().trim()) {
            case "1":
                System.out.print("Name: ");
                String name = scanner.nextLine();
                System.out.print("Type (CLASSROOM/LAB/AUDITORIUM/SPORTS_FACILITY/EQUIPMENT/MEETING_ROOM): ");
                Resource.ResourceType type = Resource.ResourceType.valueOf(scanner.nextLine().trim().toUpperCase());
                System.out.print("Capacity: ");
                int capacity = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Location: ");
                String location = scanner.nextLine();
                Resource r = resourceService.addResource(name, type, capacity, location);
                System.out.println("Added: " + r);
                break;
            case "2":
                resourceService.getAll().forEach(System.out::println);
                break;
            case "3":
                System.out.print("Resource ID: ");
                String rid = scanner.nextLine();
                System.out.print("New status (AVAILABLE/UNDER_MAINTENANCE/DECOMMISSIONED) or blank to skip: ");
                String statusInput = scanner.nextLine().trim();
                Resource.ResourceStatus status = statusInput.isEmpty() ? null : Resource.ResourceStatus.valueOf(statusInput.toUpperCase());
                Resource updated = resourceService.updateResource(rid, null, null, null, status);
                System.out.println("Updated: " + updated);
                break;
            case "4":
                System.out.print("Resource ID to delete: ");
                resourceService.deleteResource(scanner.nextLine().trim());
                System.out.println("Deleted.");
                break;
            case "5":
                System.out.print("Type: ");
                Resource.ResourceType t = Resource.ResourceType.valueOf(scanner.nextLine().trim().toUpperCase());
                System.out.print("Minimum capacity: ");
                int minCap = Integer.parseInt(scanner.nextLine().trim());
                List<Resource> matches = resourceService.findAvailableByType(t, minCap);
                if (matches.isEmpty()) System.out.println("No matching available resources.");
                matches.forEach(System.out::println);
                break;
            default: break;
        }
    }

    private static void userMenu() throws ValidationException, EntityNotFoundException {
        System.out.println("\n-- User Management --");
        System.out.println("1. Register user | 2. List all | 3. Delete user | 0. Back");
        System.out.print("Choice: ");
        switch (scanner.nextLine().trim()) {
            case "1":
                System.out.print("Name: ");
                String name = scanner.nextLine();
                System.out.print("Email: ");
                String email = scanner.nextLine();
                System.out.print("Role (STUDENT/FACULTY/ADMIN): ");
                User.Role role = User.Role.valueOf(scanner.nextLine().trim().toUpperCase());
                User u = userService.registerUser(name, email, role);
                System.out.println("Registered: " + u);
                break;
            case "2":
                userService.getAll().forEach(System.out::println);
                break;
            case "3":
                System.out.print("User ID to delete: ");
                userService.deleteUser(scanner.nextLine().trim());
                System.out.println("Deleted.");
                break;
            default: break;
        }
    }

    private static void eventMenu() {
        System.out.println("\n-- Event Scheduling --");
        System.out.println("1. Schedule event | 2. List upcoming | 3. Reschedule | 4. Cancel | 5. Events by resource | 0. Back");
        System.out.print("Choice: ");
        try {
            switch (scanner.nextLine().trim()) {
                case "1":
                    System.out.print("Title: ");
                    String title = scanner.nextLine();
                    System.out.print("Organizer user ID: ");
                    String organizerId = scanner.nextLine();
                    System.out.print("Resource ID: ");
                    String resourceId = scanner.nextLine();
                    System.out.print("Start (yyyy-MM-ddTHH:mm): ");
                    LocalDateTime start = LocalDateTime.parse(scanner.nextLine().trim(), FMT);
                    System.out.print("End (yyyy-MM-ddTHH:mm): ");
                    LocalDateTime end = LocalDateTime.parse(scanner.nextLine().trim(), FMT);
                    System.out.print("Expected attendance: ");
                    int attendance = Integer.parseInt(scanner.nextLine().trim());
                    Event event = eventSchedulerService.scheduleEvent(title, organizerId, resourceId, start, end, attendance);
                    System.out.println("Scheduled: " + event);
                    break;
                case "2":
                    eventSchedulerService.getUpcomingEvents().forEach(System.out::println);
                    break;
                case "3":
                    System.out.print("Event ID: ");
                    String eid = scanner.nextLine();
                    System.out.print("New start (yyyy-MM-ddTHH:mm): ");
                    LocalDateTime ns = LocalDateTime.parse(scanner.nextLine().trim(), FMT);
                    System.out.print("New end (yyyy-MM-ddTHH:mm): ");
                    LocalDateTime ne = LocalDateTime.parse(scanner.nextLine().trim(), FMT);
                    Event rescheduled = eventSchedulerService.rescheduleEvent(eid, ns, ne);
                    System.out.println("Rescheduled: " + rescheduled);
                    break;
                case "4":
                    System.out.print("Event ID to cancel: ");
                    eventSchedulerService.cancelEvent(scanner.nextLine().trim());
                    System.out.println("Cancelled.");
                    break;
                case "5":
                    System.out.print("Resource ID: ");
                    eventSchedulerService.getEventsForResource(scanner.nextLine().trim()).forEach(System.out::println);
                    break;
                default: break;
            }
        } catch (DateTimeParseException dtpe) {
            System.out.println("Invalid date/time format. Please use yyyy-MM-ddTHH:mm, e.g. 2026-10-01T14:00");
        } catch (ValidationException | EntityNotFoundException | ScheduleConflictException ex) {
            System.out.println("Could not complete request: " + ex.getMessage());
        }
    }

    private static void bookingMenu() {
        System.out.println("\n-- Resource Booking --");
        System.out.println("1. Create booking | 2. List all | 3. Cancel booking | 4. Bookings by user | 0. Back");
        System.out.print("Choice: ");
        try {
            switch (scanner.nextLine().trim()) {
                case "1":
                    System.out.print("Resource ID: ");
                    String resourceId = scanner.nextLine();
                    System.out.print("User ID: ");
                    String userId = scanner.nextLine();
                    System.out.print("Start (yyyy-MM-ddTHH:mm): ");
                    LocalDateTime start = LocalDateTime.parse(scanner.nextLine().trim(), FMT);
                    System.out.print("End (yyyy-MM-ddTHH:mm): ");
                    LocalDateTime end = LocalDateTime.parse(scanner.nextLine().trim(), FMT);
                    System.out.print("Purpose: ");
                    String purpose = scanner.nextLine();
                    Booking booking = bookingService.createBooking(resourceId, userId, start, end, purpose);
                    System.out.println("Booked: " + booking);
                    break;
                case "2":
                    bookingService.getAll().forEach(System.out::println);
                    break;
                case "3":
                    System.out.print("Booking ID to cancel: ");
                    bookingService.cancelBooking(scanner.nextLine().trim());
                    System.out.println("Cancelled.");
                    break;
                case "4":
                    System.out.print("User ID: ");
                    bookingService.getBookingsForUser(scanner.nextLine().trim()).forEach(System.out::println);
                    break;
                default: break;
            }
        } catch (DateTimeParseException dtpe) {
            System.out.println("Invalid date/time format. Please use yyyy-MM-ddTHH:mm");
        } catch (ValidationException | EntityNotFoundException | ScheduleConflictException ex) {
            System.out.println("Could not complete request: " + ex.getMessage());
        }
    }

    private static void reportMenu() {
        System.out.println("\n-- Reports & Analytics --");
        System.out.println("1. Utilization hours by resource | 2. Busiest resources | 3. Event load by organizer"
                + " | 4. Event status breakdown | 5. Average attendance | 0. Back");
        System.out.print("Choice: ");
        switch (scanner.nextLine().trim()) {
            case "1":
                reportService.getUtilizationHoursByResource().forEach((k, v) -> System.out.printf("%s : %.1f hrs%n", k, v));
                break;
            case "2":
                for (Map.Entry<String, Long> e : reportService.getBusiestResources(5)) {
                    System.out.println(e.getKey() + " : " + e.getValue() + " event(s)");
                }
                break;
            case "3":
                reportService.getEventLoadByOrganizer().forEach((k, v) -> System.out.println(k + " : " + v + " event(s)"));
                break;
            case "4":
                reportService.getEventStatusBreakdown().forEach((k, v) -> System.out.println(k + " : " + v));
                break;
            case "5":
                System.out.printf("Average expected attendance: %.1f%n", reportService.getAverageAttendance());
                break;
            default: break;
        }
    }

    /**
     * Seeds a handful of demo records the first time the app runs so the
     * scheduler is immediately explorable, without requiring manual data entry.
     */
    private static void seedDemoDataIfEmpty() {
        try {
            if (resourceService.getAll().isEmpty()) {
                resourceService.addResource("Seminar Hall A", Resource.ResourceType.AUDITORIUM, 200, "Block A - Ground Floor");
                resourceService.addResource("Computer Lab 1", Resource.ResourceType.LAB, 60, "Block C - 2nd Floor");
                resourceService.addResource("Room 101", Resource.ResourceType.CLASSROOM, 40, "Block B - 1st Floor");
                Logger.info("Seeded demo resources.");
            }
            if (userService.getAll().isEmpty()) {
                userService.registerUser("Dr. Anita Sharma", "anita.sharma@college.edu", User.Role.FACULTY);
                userService.registerUser("Rahul Verma", "rahul.verma@college.edu", User.Role.STUDENT);
                Logger.info("Seeded demo users.");
            }
        } catch (ValidationException e) {
            Logger.warn("Demo seeding skipped: " + e.getMessage());
        }
    }
}
