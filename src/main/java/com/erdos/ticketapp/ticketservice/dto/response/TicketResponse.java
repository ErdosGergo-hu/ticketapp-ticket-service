package com.erdos.ticketapp.ticketservice.dto.response;

import com.erdos.ticketapp.ticketservice.model.TicketStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID eventId,
        UUID ownerId,
        UUID orderId,
        String ticketType,
        BigDecimal pricePaid,
        String currency,
        String code,
        TicketStatus status,
        OffsetDateTime issuedAt,
        OffsetDateTime checkedInAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Long version
) {
}
