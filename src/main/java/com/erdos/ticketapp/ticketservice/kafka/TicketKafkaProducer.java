package com.erdos.ticketapp.ticketservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketKafkaProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${app.kafka.notification-topic}")
    private String notificationTopic;

    public void sendTicketIssued(UUID eventId, String ticketCode) {
        send(eventId, "TICKET_ISSUED", "Ticket issued! Ticket code: " + ticketCode);
    }

    private void send(UUID eventId, String type, String message) {
        NotificationEvent notificationEvent = new NotificationEvent(eventId, type, message, Instant.now());

        kafkaTemplate.send(notificationTopic, eventId.toString(), notificationEvent)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Kafka notification {} could not be sent for event {}", type, eventId, exception);
                        return;
                    }

                    log.info("Kafka notification {} sent for event {} to partition {} at offset {}",
                            type,
                            eventId,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }
}
