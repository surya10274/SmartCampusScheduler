package com.campus.scheduler.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Minimal, dependency-free logging utility that writes timestamped
 * log lines to both the console and a rolling log file (logs/app.log).
 * Used across the service layer for monitoring and error handling traceability.
 */
public final class Logger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = LOG_DIR + "/app.log";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {
    }

    public enum Level {
        INFO, WARN, ERROR
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void warn(String message) {
        log(Level.WARN, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    private static synchronized void log(Level level, String message) {
        String line = String.format("[%s] [%s] %s", LocalDateTime.now().format(FMT), level, message);
        System.out.println(line);
        try {
            java.io.File dir = new java.io.File(LOG_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                pw.println(line);
            }
        } catch (IOException e) {
            // Fall back silently to console-only logging if the file system is unavailable.
            System.out.println("[LOGGER-WARNING] Could not write to log file: " + e.getMessage());
        }
    }
}
