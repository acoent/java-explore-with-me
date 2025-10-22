package ru.practicum.ewm.repository.specification;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;

import java.time.LocalDateTime;
import java.util.Collection;

public final class EventSpecifications {

    private EventSpecifications() {
    }

    public static Specification<Event> hasInitiatorIds(Collection<Long> userIds) {
        return (root, query, cb) -> {
            if (userIds == null || userIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("initiator").get("id").in(userIds);
        };
    }

    public static Specification<Event> hasStates(Collection<EventState> states) {
        return (root, query, cb) -> {
            if (states == null || states.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("state").in(states);
        };
    }

    public static Specification<Event> hasCategories(Collection<Long> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.join("category", JoinType.LEFT).get("id").in(categoryIds);
        };
    }

    public static Specification<Event> eventDateBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) {
                return cb.conjunction();
            }
            if (start != null && end != null) {
                return cb.between(root.get("eventDate"), start, end);
            }
            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("eventDate"), start);
            }
            return cb.lessThanOrEqualTo(root.get("eventDate"), end);
        };
    }

    public static Specification<Event> isPaid(Boolean paid) {
        return (root, query, cb) -> {
            if (paid == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("paid"), paid);
        };
    }

    public static Specification<Event> hasText(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + text.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("annotation")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Event> isPublished() {
        return (root, query, cb) -> cb.equal(root.get("state"), EventState.PUBLISHED);
    }
}

