package com.erdos.ticketapp.ticketservice.repository;

import com.erdos.ticketapp.ticketservice.model.Ticket;
import com.erdos.ticketapp.ticketservice.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository
        extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    long countByEventIdAndStatusIn(UUID eventId, Collection<TicketStatus> statuses);

    Optional<Ticket> findById(UUID uuid);

    List<Ticket> findByOwnerId(UUID ownerId);
}
