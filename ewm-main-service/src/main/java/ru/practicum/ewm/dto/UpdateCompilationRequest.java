package ru.practicum.ewm.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class UpdateCompilationRequest {
    List<Long> events;
    Boolean pinned;
    @Size(min = 1, max = 128)
    String title;
}

