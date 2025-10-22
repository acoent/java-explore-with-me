package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class NewCompilationDto {

    List<Long> events;

    @Builder.Default
    Boolean pinned = Boolean.FALSE;

    @NotBlank
    @Size(min = 1, max = 128)
    String title;
}

