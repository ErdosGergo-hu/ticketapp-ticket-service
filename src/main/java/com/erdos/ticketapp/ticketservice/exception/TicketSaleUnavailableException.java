package com.erdos.ticketapp.ticketservice.exception;

import java.util.UUID;

public class TicketSaleUnavailableException extends RuntimeException {

    public TicketSaleUnavailableException(UUID eventId, String reason) {
        super(eventId == null
                ? reason
                : "Tickets cannot be sold for event " + eventId + ": " + reason);
    }
}
