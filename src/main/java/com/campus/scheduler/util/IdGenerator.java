package com.campus.scheduler.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates short, prefixed, human-readable unique IDs (e.g. R001, E014, U007, B023)
 * for each entity type instead of long UUIDs, to keep console output and file
 * records easy to read for a campus-scale dataset.
 */
public final class IdGenerator {

    private static final AtomicInteger resourceCounter = new AtomicInteger(0);
    private static final AtomicInteger eventCounter = new AtomicInteger(0);
    private static final AtomicInteger userCounter = new AtomicInteger(0);
    private static final AtomicInteger bookingCounter = new AtomicInteger(0);

    private IdGenerator() {
    }

    public static String nextResourceId() {
        return String.format("R%03d", resourceCounter.incrementAndGet());
    }

    public static String nextEventId() {
        return String.format("E%03d", eventCounter.incrementAndGet());
    }

    public static String nextUserId() {
        return String.format("U%03d", userCounter.incrementAndGet());
    }

    public static String nextBookingId() {
        return String.format("B%03d", bookingCounter.incrementAndGet());
    }

    /** Allows counters to be advanced past the highest ID loaded from disk on startup. */
    public static void primeResourceCounter(int highest) {
        resourceCounter.set(Math.max(resourceCounter.get(), highest));
    }

    public static void primeEventCounter(int highest) {
        eventCounter.set(Math.max(eventCounter.get(), highest));
    }

    public static void primeUserCounter(int highest) {
        userCounter.set(Math.max(userCounter.get(), highest));
    }

    public static void primeBookingCounter(int highest) {
        bookingCounter.set(Math.max(bookingCounter.get(), highest));
    }
}
