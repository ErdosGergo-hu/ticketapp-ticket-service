package com.erdos.ticketapp.ticketservice.kafka;

import java.time.Instant;
import java.util.UUID;

public record NotificationEvent(
        UUID eventId,
        String type,
        String message,
        Instant occurredAt) {
}
