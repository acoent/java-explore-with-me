package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.CommentStatus;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    Page<Comment> findAllByEventIdAndStatus(long eventId, CommentStatus status, Pageable pageable);

    Optional<Comment> findByIdAndAuthorId(long id, long authorId);

    Page<Comment> findAllByAuthorId(long authorId, Pageable pageable);
}

