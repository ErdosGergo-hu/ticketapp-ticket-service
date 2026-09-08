package com.erdos.ticketapp.ticketservice.exception;

import java.util.UUID;

public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(UUID eventId) {
        super("Ticket not found: " + eventId);
    }
}
