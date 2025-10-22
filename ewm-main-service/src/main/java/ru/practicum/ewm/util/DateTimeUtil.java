package ru.practicum.ewm.util;

import ru.practicum.stats.dto.StatsConstants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateTimeUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(StatsConstants.DATE_TIME_FORMAT);

    private DateTimeUtil() {
    }

    public static LocalDateTime parseOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date-time format. Expected pattern: "
                    + StatsConstants.DATE_TIME_FORMAT);
        }
    }

    public static String format(LocalDateTime value) {
        return value == null ? null : value.format(FORMATTER);
    }
}

