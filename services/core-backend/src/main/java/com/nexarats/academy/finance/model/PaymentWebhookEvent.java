package com.nexarats.academy.finance.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_webhook_events", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "event_id"})
})
public class PaymentWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "provider", nullable = false, length = 32)
    private String provider;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(name = "payload_hash", nullable = false, length = 128)
    private String payloadHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PaymentWebhookStatus status = PaymentWebhookStatus.PROCESSED;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt = Instant.now();

    public PaymentWebhookEvent() {
    }

    public PaymentWebhookEvent(String provider, String eventId, String eventType, String payloadHash, PaymentWebhookStatus status) {
        this.provider = provider;
        this.eventId = eventId;
        this.eventType = eventType;
        this.payloadHash = payloadHash;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public PaymentWebhookStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentWebhookStatus status) {
        this.status = status;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
