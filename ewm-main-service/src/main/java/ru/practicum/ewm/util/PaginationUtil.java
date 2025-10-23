package ru.practicum.ewm.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtil {

    private PaginationUtil() {
    }

    public static Pageable offsetPageable(int from, int size) {
        return offsetPageable(from, size, Sort.unsorted());
    }

    public static Pageable offsetPageable(int from, int size, Sort sort) {
        if (from < 0) {
            throw new IllegalArgumentException("Parameter 'from' must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Parameter 'size' must be greater than 0");
        }
        int page = from / size;
        return PageRequest.of(page, size, sort == null ? Sort.unsorted() : sort);
    }
}

