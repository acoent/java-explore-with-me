package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.practicum.ewm.model.CommentStatus;

@Value
public class UpdateCommentAdminRequest {

    @NotNull
    CommentStatus status;
}

