package com.erdos.ticketapp.ticketservice.exception;

public class TicketInvalidStateException extends RuntimeException {

    public TicketInvalidStateException(String message) {
        super(message);
    }
}
