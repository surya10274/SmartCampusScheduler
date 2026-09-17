package com.campus.scheduler.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Generic flat-file persistence helper. Each entity type is stored as a
 * plain-text, pipe-delimited file under the data/ directory. This keeps the
 * project dependency-free (no external database/driver required) while still
 * demonstrating a clean separation between the persistence layer and the
 * business/service layer.
 */
public final class FileStorageUtil {

    private static final String DATA_DIR = "data";

    private FileStorageUtil() {
    }

    public static <T> List<T> loadAll(String fileName, Function<String, T> parser) {
        List<T> result = new ArrayList<>();
        Path path = Paths.get(DATA_DIR, fileName);
        if (!Files.exists(path)) {
            return result;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        result.add(parser.apply(line));
                    } catch (Exception ex) {
                        Logger.warn("Skipping corrupt record in " + fileName + ": " + line);
                    }
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to read " + fileName + ": " + e.getMessage());
        }
        return result;
    }

    public static <T> void saveAll(String fileName, List<T> items, Function<T, String> serializer) {
        try {
            Path dir = Paths.get(DATA_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path path = dir.resolve(fileName);
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (T item : items) {
                    writer.write(serializer.apply(item));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to write " + fileName + ": " + e.getMessage());
        }
    }
}
