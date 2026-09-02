package com.erdos.ticketapp.ticketservice.search.specification;

import com.erdos.ticketapp.ticketservice.model.Ticket;
import com.erdos.ticketapp.ticketservice.search.criteria.TicketSearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class TicketSpecification extends AbstractSpecification<TicketSearchCriteria, Ticket> {
}
