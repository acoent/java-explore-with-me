package ru.practicum.ewm.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CategoryDto {
    Long id;
    String name;
}

