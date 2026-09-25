package com.erdos.ticketapp.ticketservice.service;

import com.erdos.ticketapp.ticketservice.client.EventClient;
import com.erdos.ticketapp.ticketservice.client.dto.EventStatus;
import com.erdos.ticketapp.ticketservice.client.dto.EventTicketingInfoResponse;
import com.erdos.ticketapp.ticketservice.dto.request.TicketPurchaseRequest;
import com.erdos.ticketapp.ticketservice.dto.response.TicketResponse;
import com.erdos.ticketapp.ticketservice.enums.OutboxStatus;
import com.erdos.ticketapp.ticketservice.exception.*;
import com.erdos.ticketapp.ticketservice.kafka.NotificationEvent;
import com.erdos.ticketapp.ticketservice.mapper.TicketMapper;
import com.erdos.ticketapp.ticketservice.model.OutboxEvent;
import com.erdos.ticketapp.ticketservice.model.Ticket;
import com.erdos.ticketapp.ticketservice.model.TicketStatus;
import com.erdos.ticketapp.ticketservice.repository.OutboxEventRepository;
import com.erdos.ticketapp.ticketservice.repository.TicketRepository;
import com.erdos.ticketapp.ticketservice.search.criteria.TicketSearchCriteria;
import com.erdos.ticketapp.ticketservice.search.specification.TicketSpecification;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository repository;

    private final TicketMapper ticketMapper;

    private final TicketSpecification ticketSpecification;

    private final EventClient eventClient;

    private final OutboxEventRepository outboxRepository;

    private final ObjectMapper objectMapper;

    public Page<TicketResponse> search(
            TicketSearchCriteria ticketSearchCriteria,
            Pageable pageable) {
        return repository.findAll(ticketSpecification.build(ticketSearchCriteria), pageable)
                .map(ticketMapper::toResponse);
    }

    @Transactional
    public synchronized TicketResponse purchase(String idempotencyKey, TicketPurchaseRequest request) {
        String validatedKey = validateIdempotencyKey(idempotencyKey);

        Optional<Ticket> existing = repository.findByIdempotencyKey(validatedKey);

        if (existing.isPresent()) {
            validateSamePurchase(existing.get(), request);
            return ticketMapper.toResponse(existing.get());
        }

        EventTicketingInfoResponse event = eventClient.getTicketingInfo(request.eventId());

        validateEventCanSellTickets(event);

        long soldTickets = repository.countByEventIdAndStatusIn(
                request.eventId(),
                List.of(TicketStatus.RESERVED, TicketStatus.ACTIVE, TicketStatus.USED));

        if (soldTickets >= event.capacity()) {
            throw new TicketsSoldOutException(request.eventId());
        }

        Ticket ticket = Ticket.builder()
                .eventId(event.eventId())
                .ownerId(request.ownerId())
                .orderId(UUID.randomUUID())
                .ticketType("STANDARD")
                .pricePaid(event.basePrice())
                .currency(event.currency())
                .code(UUID.randomUUID().toString())
                .status(TicketStatus.ACTIVE)
                .idempotencyKey(validatedKey)
                .issuedAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Ticket savedTicket = repository.save(ticket);

        outboxRepository.save(createOutboxEvent(savedTicket));

        return ticketMapper.toResponse(savedTicket);
    }

    private OutboxEvent createOutboxEvent(Ticket ticket) {
        UUID messageId = UUID.randomUUID();

        NotificationEvent notification = new NotificationEvent(
                messageId,
                ticket.getEventId(),
                "TICKET_ISSUED",
                "Ticket issued! Ticket code: " + ticket.getCode(),
                Instant.now()
        );

        String payload;

        try {
            payload = objectMapper.writeValueAsString(notification);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Could not serialize notification event",
                    exception
            );
        }

        return OutboxEvent.builder()
                .id(messageId)
                .aggregateId(ticket.getId())
                .eventType(notification.type())
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .attemptCount(0)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private void validateEventCanSellTickets(EventTicketingInfoResponse event) {
        if (event == null || event.eventId() == null) {
            throw new TicketSaleUnavailableException(null, "Event ticketing information is missing");
        }

        if (!EventStatus.PUBLISHED.equals(event.status())) {
            throw new TicketSaleUnavailableException(
                    event.eventId(),
                    "Event is not published. Current status: " + event.status());
        }

        OffsetDateTime now = OffsetDateTime.now();

        if (event.ticketSalesStart() != null && now.isBefore(event.ticketSalesStart())) {
            throw new TicketSaleUnavailableException(event.eventId(), "Ticket sales have not started yet");
        }

        if (event.ticketSalesEnd() != null && now.isAfter(event.ticketSalesEnd())) {
            throw new TicketSaleUnavailableException(event.eventId(), "Ticket sales have ended");
        }

        if (event.capacity() == null || event.capacity() < 1) {
            throw new TicketSaleUnavailableException(event.eventId(), "Event has no sellable capacity");
        }
    }

    @Transactional
    public TicketResponse cancel(UUID uuid) {
        Ticket ticket = repository.findById(uuid)
                .orElseThrow(() -> new TicketNotFoundException(uuid));

        if (TicketStatus.CANCELLED.equals(ticket.getStatus())) {
            return ticketMapper.toResponse(ticket);
        }

        if(!TicketStatus.ACTIVE.equals(ticket.getStatus())) {
            throw new TicketInvalidStateException("Cannot cancel the ticket because the status is not ACTIVE");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setUpdatedAt(OffsetDateTime.now());
        Ticket saved = repository.save(ticket);

        return ticketMapper.toResponse(saved);
    }

    @Transactional
    public TicketResponse refund(UUID uuid) {
        Ticket ticket = repository.findById(uuid)
                .orElseThrow(() -> new TicketNotFoundException(uuid));

        if (TicketStatus.REFUNDED.equals(ticket.getStatus())) {
            return ticketMapper.toResponse(ticket);
        }

        if(!TicketStatus.ACTIVE.equals(ticket.getStatus()) && !TicketStatus.CANCELLED.equals(ticket.getStatus())) {
            throw new TicketInvalidStateException("Cannot refund the ticket because the status is not ACTIVE/CANCEL");
        }

        ticket.setStatus(TicketStatus.REFUNDED);
        ticket.setUpdatedAt(OffsetDateTime.now());
        Ticket saved = repository.save(ticket);

        return ticketMapper.toResponse(saved);
    }

    @Transactional
    public TicketResponse checkIn(UUID id) {
        Ticket ticket = repository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        if (TicketStatus.USED.equals(ticket.getStatus())) {
            return ticketMapper.toResponse(ticket);
        }

        if (!TicketStatus.ACTIVE.equals(ticket.getStatus())) {
            throw new TicketCheckInException(id, ticket.getStatus());
        }

        OffsetDateTime now = OffsetDateTime.now();
        ticket.setStatus(TicketStatus.USED);
        ticket.setCheckedInAt(now);
        ticket.setUpdatedAt(now);

        Ticket saved = repository.save(ticket);
        return ticketMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByOwnerId(UUID ownerId) {
        List<Ticket> tickets = repository.findByOwnerId(ownerId);

        return tickets.stream().map(ticketMapper::toResponse).toList();
    }

    private String validateIdempotencyKey(String key) {
        if (key == null || key.isBlank()) {
            throw new InvalidIdempotencyKeyException(
                    "Idempotency-Key header is required"
            );
        }

        if (key.length() > 100) {
            throw new InvalidIdempotencyKeyException(
                    "Idempotency-Key cannot be longer than 100 characters"
            );
        }

        return key.trim();
    }

    private void validateSamePurchase(
            Ticket existing,
            TicketPurchaseRequest request
    ) {
        boolean sameRequest =
                existing.getEventId().equals(request.eventId())
                        && existing.getOwnerId().equals(request.ownerId())
                        && existing.getTicketType().equals(request.ticketType());

        if (!sameRequest) {
            throw new InvalidIdempotencyKeyException(
                    "Idempotency-Key was already used with a different request"
            );
        }
    }
}
