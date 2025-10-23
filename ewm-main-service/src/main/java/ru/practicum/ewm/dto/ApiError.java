package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.springframework.http.HttpStatus;
import ru.practicum.stats.dto.StatsConstants;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class ApiError {

    @Singular
    List<String> errors;

    String message;

    String reason;

    HttpStatus status;

    @JsonFormat(pattern = StatsConstants.DATE_TIME_FORMAT)
    LocalDateTime timestamp;
}

