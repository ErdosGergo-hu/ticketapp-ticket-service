package com.erdos.ticketapp.ticketservice.search.criteria;

import com.erdos.ticketapp.ticketservice.model.TicketStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class TicketSearchCriteria {

    private String query;
    private UUID eventId;
    private UUID ownerId;
    private UUID orderId;
    private String ticketType;
    private TicketStatus status;
}
