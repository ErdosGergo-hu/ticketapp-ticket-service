package com.erdos.ticketapp.ticketservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class TicketsSoldOutException extends RuntimeException {

    public TicketsSoldOutException(UUID eventId) {
        super("Tickets are sold out for event: " + eventId);
    }
}
