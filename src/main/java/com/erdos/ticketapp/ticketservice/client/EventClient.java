package com.erdos.ticketapp.ticketservice.client;

import com.erdos.ticketapp.ticketservice.client.dto.EventTicketingInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "event-service", url = "${clients.event-service.url}")
public interface EventClient {

    @GetMapping("/events/{id}/ticketing-info")
    EventTicketingInfoResponse getTicketingInfo(@PathVariable("id") UUID id);
}
