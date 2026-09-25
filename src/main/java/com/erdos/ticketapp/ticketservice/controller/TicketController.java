package com.erdos.ticketapp.ticketservice.controller;

import com.erdos.ticketapp.ticketservice.dto.request.TicketPurchaseRequest;
import com.erdos.ticketapp.ticketservice.dto.response.TicketResponse;
import com.erdos.ticketapp.ticketservice.search.criteria.TicketSearchCriteria;
import com.erdos.ticketapp.ticketservice.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/search")
    public Page<TicketResponse> search(
            TicketSearchCriteria ticketSearchCriteria,
            Pageable pageable) {
        return ticketService.search(ticketSearchCriteria, pageable);
    }

    @PostMapping
    public TicketResponse purchase(@RequestHeader("Idempotency-Key") String idempotencyKey, @RequestBody @Valid TicketPurchaseRequest ticketPurchaseRequest){
        return ticketService.purchase(idempotencyKey, ticketPurchaseRequest);
    }

    @PostMapping("/{id}/cancel")
    public TicketResponse cancel(@PathVariable("id") UUID id) {
        return ticketService.cancel(id);
    }

    @PostMapping("/{id}/refund")
    public TicketResponse refund(@PathVariable("id") UUID id) {
        return ticketService.refund(id);
    }

    @PostMapping("/{id}/check-in")
    public TicketResponse checkIn(@PathVariable("id") UUID id) {
        return ticketService.checkIn(id);
    }

    @GetMapping("/owner/{ownerId}")
    public List<TicketResponse> ticketByOwnerId(@PathVariable UUID ownerId) {
        return ticketService.getTicketsByOwnerId(ownerId);
    }

    @GetMapping
    public String hello() {
        return "This is the Ticket app!";
    }
}
