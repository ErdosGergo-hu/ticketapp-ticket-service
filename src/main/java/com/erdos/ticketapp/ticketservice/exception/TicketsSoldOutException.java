package com.erdos.ticketapp.ticketservice.exception;

import java.util.UUID;

public class TicketsSoldOutException extends RuntimeException {

    public TicketsSoldOutException(UUID eventId) {
        super("Tickets are sold out for event: " + eventId);
    }
}
