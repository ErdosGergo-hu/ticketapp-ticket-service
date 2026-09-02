package com.erdos.ticketapp.ticketservice.controller;

import com.erdos.ticketapp.ticketservice.dto.response.TicketResponse;
import com.erdos.ticketapp.ticketservice.search.criteria.TicketSearchCriteria;
import com.erdos.ticketapp.ticketservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/search")
    public Page<TicketResponse> search(
            TicketSearchCriteria ticketSearchCriteria,
            Pageable pageable) {
        return ticketService.search(ticketSearchCriteria, pageable);
    }

    @GetMapping
    public String hello() {
        return "This is the Ticket app!";
    }
}
