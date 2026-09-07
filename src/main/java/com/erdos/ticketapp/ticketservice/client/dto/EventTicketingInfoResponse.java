package com.erdos.ticketapp.ticketservice.client.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EventTicketingInfoResponse(
        UUID eventId,
        EventStatus status,
        OffsetDateTime ticketSalesStart,
        OffsetDateTime ticketSalesEnd,
        Integer capacity,
        BigDecimal basePrice,
        String currency) {
}
