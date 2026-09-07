package com.erdos.ticketapp.ticketservice.exception;

import com.erdos.ticketapp.ticketservice.model.TicketStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class TicketCheckInException extends RuntimeException {

    public TicketCheckInException(UUID ticketId, TicketStatus status) {
        super("Ticket " + ticketId + " cannot be checked in because its status is " + status);
    }
}
