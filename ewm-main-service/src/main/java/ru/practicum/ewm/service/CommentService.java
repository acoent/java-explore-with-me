package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.dto.UpdateCommentAdminRequest;
import ru.practicum.ewm.dto.UpdateCommentUserRequest;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.util.PaginationUtil;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public List<CommentDto> getPublishedComments(long eventId, int from, int size) {
        Event event = getPublishedEvent(eventId);
        Pageable pageable = PaginationUtil.offsetPageable(from, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        return commentRepository.findAllByEventIdAndStatus(event.getId(), CommentStatus.PUBLISHED, pageable).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    public List<CommentDto> getUserComments(long userId, int from, int size) {
        ensureUserExists(userId);
        Pageable pageable = PaginationUtil.offsetPageable(from, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        return commentRepository.findAllByAuthorId(userId, pageable).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    @Transactional
    public CommentDto createComment(long userId, long eventId, NewCommentDto dto) {
        User author = getUser(userId);
        Event event = getPublishedEvent(eventId);
        String text = dto.getText().trim();
        if (text.isEmpty()) {
            throw new ValidationException("Comment text must not be blank");
        }
        Comment comment = Comment.builder()
                .text(text)
                .author(author)
                .event(event)
                .status(CommentStatus.PENDING)
                .createdOn(LocalDateTime.now(clock))
                .build();
        Comment saved = commentRepository.save(comment);
        log.info("Created comment id={} for event {} by user {}", saved.getId(), eventId, userId);
        return CommentMapper.toDto(saved);
    }

    @Transactional
    public CommentDto updateComment(long userId, long commentId, UpdateCommentUserRequest request) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment with id=%d was not found".formatted(commentId)));
        String text = request.getText().trim();
        if (text.isEmpty()) {
            throw new ValidationException("Comment text must not be blank");
        }
        comment.setText(text);
        comment.setStatus(CommentStatus.PENDING);
        comment.setUpdatedOn(LocalDateTime.now(clock));
        Comment saved = commentRepository.save(comment);
        log.info("Updated comment id={} by user {} set status to PENDING", commentId, userId);
        return CommentMapper.toDto(saved);
    }

    @Transactional
    public void deleteComment(long userId, long commentId) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment with id=%d was not found".formatted(commentId)));
        commentRepository.deleteById(comment.getId());
        log.info("Deleted comment id={} by user {}", commentId, userId);
    }

    public List<CommentDto> getCommentsForAdmin(CommentStatus status,
                                                Long eventId,
                                                Long authorId,
                                                int from,
                                                int size) {
        Pageable pageable = PaginationUtil.offsetPageable(from, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        Specification<Comment> spec = specification(status);
        if (eventId != null) {
            Specification<Comment> byEvent = (root, query, cb) -> cb.equal(root.get("event").get("id"), eventId);
            spec = spec == null ? Specification.where(byEvent) : spec.and(byEvent);
        }
        if (authorId != null) {
            Specification<Comment> byAuthor = (root, query, cb) -> cb.equal(root.get("author").get("id"), authorId);
            spec = spec == null ? Specification.where(byAuthor) : spec.and(byAuthor);
        }
        return commentRepository.findAll(spec, pageable).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    @Transactional
    public CommentDto moderateComment(long commentId, UpdateCommentAdminRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=%d was not found".formatted(commentId)));
        CommentStatus newStatus = request.getStatus();
        if (newStatus == null) {
            throw new ValidationException("Status must be provided");
        }
        if (newStatus == CommentStatus.PENDING) {
            throw new ConflictException("Administrator cannot set status back to PENDING");
        }
        comment.setStatus(newStatus);
        comment.setUpdatedOn(LocalDateTime.now(clock));
        Comment saved = commentRepository.save(comment);
        log.info("Comment id={} moderated with status {}", commentId, newStatus);
        return CommentMapper.toDto(saved);
    }

    @Transactional
    public void deleteCommentByAdmin(long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment with id=%d was not found".formatted(commentId));
        }
        commentRepository.deleteById(commentId);
        log.info("Administrator deleted comment id={}", commentId);
    }

    private Specification<Comment> specification(CommentStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Event getPublishedEvent(long eventId) {
        return eventRepository.findById(eventId)
                .filter(event -> event.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=%d was not found".formatted(eventId)));
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=%d was not found".formatted(userId)));
    }

    private void ensureUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=%d was not found".formatted(userId));
        }
    }
}

