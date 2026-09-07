package com.erdos.ticketapp.ticketservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(UUID eventId) {
        super("Ticket not found: " + eventId);
    }
}
