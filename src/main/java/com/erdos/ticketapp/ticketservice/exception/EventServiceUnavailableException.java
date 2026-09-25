package com.erdos.ticketapp.ticketservice.exception;

import java.util.UUID;

public class EventServiceUnavailableException extends RuntimeException {

    public EventServiceUnavailableException(UUID eventId, Throwable cause) {
        super("Event information is temporarily unavailable for event " + eventId, cause);
    }
}
