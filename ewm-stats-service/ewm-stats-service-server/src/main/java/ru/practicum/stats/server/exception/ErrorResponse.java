package ru.practicum.stats.server.exception;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ErrorResponse {

    String status;
    String reason;
    String message;
    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();
}
