package com.erdos.ticketapp.ticketservice.repository;

import com.erdos.ticketapp.ticketservice.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
        SELECT *
        FROM outbox_events
        WHERE status = 'PENDING'
          AND (
              next_attempt_at IS NULL
              OR next_attempt_at <= :now
          )
        ORDER BY created_at
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch(
            @Param("now") OffsetDateTime now,
            @Param("batchSize") int batchSize
    );
}