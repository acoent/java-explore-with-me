package ru.practicum.ewm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaginationUtilTest {

    @Test
    @DisplayName("offsetPageable calculates correct page index")
    void offsetPageableCalculatesPage() {
        Pageable pageable = PaginationUtil.offsetPageable(20, 10);
        assertEquals(2, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    @DisplayName("offsetPageable rejects negative from")
    void offsetPageableRejectsNegativeFrom() {
        assertThrows(IllegalArgumentException.class, () -> PaginationUtil.offsetPageable(-1, 10));
    }

    @Test
    @DisplayName("offsetPageable rejects non positive size")
    void offsetPageableRejectsInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> PaginationUtil.offsetPageable(0, 0));
    }
}

