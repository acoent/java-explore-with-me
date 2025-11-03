package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.UpdateCommentAdminRequest;
import ru.practicum.ewm.model.CommentStatus;
import ru.practicum.ewm.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getComments(@RequestParam(required = false) CommentStatus status,
                                        @RequestParam(required = false) Long eventId,
                                        @RequestParam(required = false) Long authorId,
                                        @RequestParam(defaultValue = "0") @Min(0) int from,
                                        @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.getCommentsForAdmin(status, eventId, authorId, from, size);
    }

    @PatchMapping("/{commentId}")
    public CommentDto moderateComment(@PathVariable long commentId,
                                      @Valid @RequestBody UpdateCommentAdminRequest request) {
        return commentService.moderateComment(commentId, request);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }
}

