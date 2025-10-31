package ru.practicum.ewm.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.CommentStatus;

public final class CommentSpecifications {

    private CommentSpecifications() {
    }

    public static Specification<Comment> hasStatus(CommentStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Comment> hasEventId(Long eventId) {
        return (root, query, cb) -> {
            if (eventId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("event").get("id"), eventId);
        };
    }

    public static Specification<Comment> hasAuthorId(Long authorId) {
        return (root, query, cb) -> {
            if (authorId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("author").get("id"), authorId);
        };
    }
}
