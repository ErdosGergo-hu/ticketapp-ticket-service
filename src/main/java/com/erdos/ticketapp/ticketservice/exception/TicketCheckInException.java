package com.erdos.ticketapp.ticketservice.exception;

import com.erdos.ticketapp.ticketservice.model.TicketStatus;

import java.util.UUID;

public class TicketCheckInException extends RuntimeException {

    public TicketCheckInException(UUID ticketId, TicketStatus status) {
        super("Ticket " + ticketId + " cannot be checked in because its status is " + status);
    }
}
