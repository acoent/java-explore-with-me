package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import ru.practicum.ewm.model.CommentStatus;

import java.time.LocalDateTime;

@Value
@Builder
public class CommentDto {

    Long id;
    String text;
    Long eventId;
    UserShortDto author;
    CommentStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdOn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedOn;
}

