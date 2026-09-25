package com.erdos.ticketapp.ticketservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false, length = 100)
    private String ticketType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePaid;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime issuedAt;

    private OffsetDateTime checkedInAt;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Version
    private Long version;
}
