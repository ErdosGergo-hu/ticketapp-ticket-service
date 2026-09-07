package com.erdos.ticketapp.ticketservice.dto.request;

import java.util.UUID;

public record TicketPurchaseRequest(
        UUID eventId,
        UUID ownerId,
        String ticketType
) {
}
