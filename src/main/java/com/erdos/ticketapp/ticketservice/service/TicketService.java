package com.erdos.ticketapp.ticketservice.service;

import com.erdos.ticketapp.ticketservice.dto.response.TicketResponse;
import com.erdos.ticketapp.ticketservice.mapper.TicketMapper;
import com.erdos.ticketapp.ticketservice.repository.TicketRepository;
import com.erdos.ticketapp.ticketservice.search.criteria.TicketSearchCriteria;
import com.erdos.ticketapp.ticketservice.search.specification.TicketSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository repository;
    private final TicketMapper ticketMapper;
    private final TicketSpecification ticketSpecification;

    public Page<TicketResponse> search(
            TicketSearchCriteria ticketSearchCriteria,
            Pageable pageable) {
        return repository.findAll(ticketSpecification.build(ticketSearchCriteria), pageable)
                .map(ticketMapper::toResponse);
    }
}
