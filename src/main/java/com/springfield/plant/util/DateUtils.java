package com.springfield.plant.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date "utilities".
 * ☢️ LEGACY ALERT: a static shared SimpleDateFormat is NOT thread-safe.
 * Under load two threads will happily corrupt each other's dates.
 * Modern fix: java.time.LocalDateTime + DateTimeFormatter.
 */
public final class DateUtils {

    // Shared mutable formatter. Homer said it was fine.
    public static final SimpleDateFormat PLANT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private DateUtils() {
    }

    public static String format(Date date) {
        if (date == null) {
            return "never (probably fine)";
        }
        return PLANT_FORMAT.format(date);
    }

    public static Date parse(String text) {
        try {
            return PLANT_FORMAT.parse(text);
        } catch (ParseException e) {
            // ☢️ LEGACY ALERT: swallow the exception, return "now". Classic.
            return new Date();
        }
    }

    public static Date daysAgo(int days) {
        // ☢️ LEGACY ALERT: manual millisecond math instead of java.time.
        return new Date(System.currentTimeMillis() - (long) days * 24L * 60L * 60L * 1000L);
    }
}
