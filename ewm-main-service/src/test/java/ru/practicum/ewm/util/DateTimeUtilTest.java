package ru.practicum.ewm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilTest {

    @Test
    @DisplayName("parseOrNull parses valid string")
    void parseOrNullParses() {
        LocalDateTime dateTime = DateTimeUtil.parseOrNull("2024-07-20 15:30:00");
        assertEquals(2024, dateTime.getYear());
        assertEquals(7, dateTime.getMonthValue());
        assertEquals(20, dateTime.getDayOfMonth());
        assertEquals(15, dateTime.getHour());
        assertEquals(30, dateTime.getMinute());
    }

    @Test
    @DisplayName("parseOrNull returns null for blank input")
    void parseOrNullReturnsNullForBlank() {
        assertNull(DateTimeUtil.parseOrNull(""));
        assertNull(DateTimeUtil.parseOrNull(null));
    }

    @Test
    @DisplayName("parseOrNull throws for invalid format")
    void parseOrNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtil.parseOrNull("invalid"));
    }
}

