package com.springfield.plant.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Date utilities.
 */
public final class DateUtils {

    public static final DateTimeFormatter PLANT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateUtils() {
    }

    public static String format(Instant timestamp) {
        if (timestamp == null) {
            return "never (probably fine)";
        }
        return PLANT_FORMAT.format(timestamp.atOffset(ZoneOffset.UTC));
    }

    public static Instant parse(String text) {
        try {
            return LocalDateTime.parse(text, PLANT_FORMAT).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid plant timestamp: " + text, e);
        }
    }

    public static Instant daysAgo(int days) {
        return Instant.now().minus(days, ChronoUnit.DAYS);
    }
}
