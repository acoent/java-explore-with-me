package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import ru.practicum.ewm.model.CommentStatus;

import java.time.LocalDateTime;

@Value
@Builder
public class CommentDto {

    private final Long id;
    private final String text;
    private final Long eventId;
    private final UserShortDto author;
    private final CommentStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdOn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime updatedOn;
}
