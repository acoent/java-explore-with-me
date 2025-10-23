package ru.practicum.ewm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "requests", uniqueConstraints = {
        @UniqueConstraint(name = "uk_requests_event_requester", columnNames = {"event_id", "requester_id"})
}, indexes = {
        @Index(name = "idx_requests_event_id", columnList = "event_id"),
        @Index(name = "idx_requests_requester_id", columnList = "requester_id"),
        @Index(name = "idx_requests_status", columnList = "status")
})
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Column(nullable = false)
    private LocalDateTime created;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RequestStatus status;
}

