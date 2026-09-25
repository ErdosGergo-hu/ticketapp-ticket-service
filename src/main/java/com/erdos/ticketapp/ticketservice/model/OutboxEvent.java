package com.erdos.ticketapp.ticketservice.model;

import com.erdos.ticketapp.ticketservice.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OutboxStatus status;

    @Column(nullable = false)
    private Integer attemptCount;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime nextAttemptAt;

    private OffsetDateTime publishedAt;

    @Column(length = 2000)
    private String lastError;
}
