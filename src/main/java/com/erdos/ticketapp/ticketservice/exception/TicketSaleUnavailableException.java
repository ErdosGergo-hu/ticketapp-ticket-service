package com.erdos.ticketapp.ticketservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class TicketSaleUnavailableException extends RuntimeException {

    public TicketSaleUnavailableException(UUID eventId, String reason) {
        super(eventId == null
                ? reason
                : "Tickets cannot be sold for event " + eventId + ": " + reason);
    }
}
