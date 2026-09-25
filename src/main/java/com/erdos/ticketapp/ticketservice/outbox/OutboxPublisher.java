package com.erdos.ticketapp.ticketservice.outbox;

import com.erdos.ticketapp.ticketservice.enums.OutboxStatus;
import com.erdos.ticketapp.ticketservice.kafka.NotificationEvent;
import com.erdos.ticketapp.ticketservice.model.OutboxEvent;
import com.erdos.ticketapp.ticketservice.repository.OutboxEventRepository;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BATCH_SIZE = 100;

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.notification-topic}")
    private String notificationTopic;

    @Value("${app.outbox.retry-delay-seconds:30}")
    private long retryDelaySeconds;

    @Scheduled(fixedDelayString = "${app.outbox.publish-delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = repository.lockNextBatch(
                OffsetDateTime.now(),
                BATCH_SIZE
        );

        for (OutboxEvent outbox : events) {
            publish(outbox);
        }
    }

    private void publish(OutboxEvent outbox) {
        outbox.setStatus(OutboxStatus.PROCESSING);
        outbox.setAttemptCount(outbox.getAttemptCount() + 1);

        try {
            NotificationEvent event = objectMapper.readValue(
                    outbox.getPayload(),
                    NotificationEvent.class
            );

            kafkaTemplate.send(notificationTopic, event.eventId().toString(), event).get(3, TimeUnit.SECONDS);

            outbox.setStatus(OutboxStatus.PUBLISHED);
            outbox.setPublishedAt(OffsetDateTime.now());
            outbox.setNextAttemptAt(null);
            outbox.setLastError(null);

            log.info("Outbox event {} published", outbox.getId());
        } catch (Exception exception) {
            handleFailure(outbox, exception);
        }
    }

    private void handleFailure(
            OutboxEvent outbox,
            Exception exception
    ) {
        String error = exception.getMessage();

        if (error != null && error.length() > 2000) {
            error = error.substring(0, 2000);
        }

        outbox.setLastError(error);

        if (outbox.getAttemptCount() >= MAX_ATTEMPTS) {
            outbox.setStatus(OutboxStatus.DEAD);
            outbox.setNextAttemptAt(null);

            log.error(
                    "Outbox event {} moved to DEAD after {} attempts",
                    outbox.getId(),
                    outbox.getAttemptCount(),
                    exception
            );
        } else {
            outbox.setStatus(OutboxStatus.PENDING);
            outbox.setNextAttemptAt(
                    OffsetDateTime.now().plusSeconds(retryDelaySeconds)
            );

            log.warn(
                    "Outbox event {} could not be published; retry scheduled",
                    outbox.getId(),
                    exception
            );
        }
    }
}