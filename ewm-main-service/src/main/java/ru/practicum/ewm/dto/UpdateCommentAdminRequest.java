package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.practicum.ewm.model.CommentStatus;

@Value
public class UpdateCommentAdminRequest {

    @NotNull
    private final CommentStatus status;

    @JsonCreator
    public UpdateCommentAdminRequest(@JsonProperty("status") CommentStatus status) {
        this.status = status;
    }
}
