package com.erdos.ticketapp.ticketservice.client;

import com.erdos.ticketapp.ticketservice.exception.EventServiceUnavailableException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class EventClientFallbackFactory implements FallbackFactory<EventClient> {

    @Override
    public EventClient create(Throwable cause) {
        return eventId -> {
            throw new EventServiceUnavailableException(eventId, cause);
        };
    }
}
