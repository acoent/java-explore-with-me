package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.dto.UpdateCommentUserRequest;
import ru.practicum.ewm.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Validated
public class UserCommentController {

    private final CommentService commentService;

    @GetMapping("/comments")
    public List<CommentDto> getUserComments(@PathVariable long userId,
                                            @RequestParam(defaultValue = "0") @Min(0) int from,
                                            @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.getUserComments(userId, from, size);
    }

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable long userId,
                                 @PathVariable long eventId,
                                 @Valid @RequestBody NewCommentDto dto) {
        return commentService.createComment(userId, eventId, dto);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto updateComment(@PathVariable long userId,
                                    @PathVariable long commentId,
                                    @Valid @RequestBody UpdateCommentUserRequest request) {
        return commentService.updateComment(userId, commentId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long userId,
                              @PathVariable long commentId) {
        commentService.deleteComment(userId, commentId);
    }
}

