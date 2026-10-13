package com.nexarats.academy.finance.service;

import com.nexarats.academy.finance.event.PaymentCompletedEvent;
import com.nexarats.academy.finance.model.*;
import com.nexarats.academy.finance.repository.PaymentOrderRepository;
import com.nexarats.academy.finance.repository.PaymentTransactionRepository;
import com.nexarats.academy.finance.repository.PaymentWebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentOrderService {

    private static final Logger log = LoggerFactory.getLogger(PaymentOrderService.class);

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentWebhookEventRepository paymentWebhookEventRepository;
    private final InvoiceService invoiceService;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<PaymentProvider, PaymentGateway> gateways;

    public PaymentOrderService(PaymentOrderRepository paymentOrderRepository,
                               PaymentTransactionRepository paymentTransactionRepository,
                               PaymentWebhookEventRepository paymentWebhookEventRepository,
                               InvoiceService invoiceService,
                               ApplicationEventPublisher eventPublisher,
                               List<PaymentGateway> gatewayList) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentWebhookEventRepository = paymentWebhookEventRepository;
        this.invoiceService = invoiceService;
        this.eventPublisher = eventPublisher;
        this.gateways = gatewayList.stream()
                .collect(Collectors.toMap(PaymentGateway::getProvider, Function.identity()));
    }

    @Transactional
    public PaymentOrderCreationResult createOrder(
            UUID tenantId,
            UUID learnerUserId,
            UUID applicationId,
            UUID offerId,
            UUID cohortId,
            UUID reservationId,
            long amountCents,
            String currency,
            PaymentProvider provider,
            String idempotencyKey,
            String customerEmail,
            String customerName) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<PaymentOrder> existingOpt = paymentOrderRepository.findByIdempotencyKey(idempotencyKey);
            if (existingOpt.isPresent()) {
                PaymentOrder existing = existingOpt.get();
                log.info("Returning existing payment order {} for idempotency key {}", existing.getOrderNumber(), idempotencyKey);
                return new PaymentOrderCreationResult(existing, existing.getProviderOrderId(), null, null);
            }
        }

        PaymentProvider effectiveProvider = (provider != null) ? provider : PaymentProvider.TEST_GATEWAY;
        PaymentGateway gateway = gateways.get(effectiveProvider);
        if (gateway == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment provider: " + effectiveProvider);
        }

        String orderNum = "ORD-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String effectiveKey = (idempotencyKey != null && !idempotencyKey.isBlank()) ? idempotencyKey : UUID.randomUUID().toString();

        PaymentOrder order = new PaymentOrder(orderNum, amountCents, (currency != null ? currency.toUpperCase() : "USD"), effectiveProvider, effectiveKey);
        order.setTenantId(tenantId);
        order.setLearnerUserId(learnerUserId);
        order.setApplicationId(applicationId);
        order.setOfferId(offerId);
        order.setCohortId(cohortId);
        order.setReservationId(reservationId);
        order.setStatus(PaymentOrderStatus.CREATED);

        PaymentOrder saved = paymentOrderRepository.save(order);

        // Initialize with gateway
        PaymentGateway.GatewayOrderResult gatewayResult = gateway.createOrder(saved, customerEmail, customerName);
        saved.setProviderOrderId(gatewayResult.providerOrderId());
        saved.setStatus(PaymentOrderStatus.PENDING);
        paymentOrderRepository.save(saved);

        log.info("Initialized payment order {} (id: {}) with provider {}", orderNum, saved.getId(), effectiveProvider);
        return new PaymentOrderCreationResult(saved, gatewayResult.providerOrderId(), gatewayResult.clientSecret(), gatewayResult.checkoutUrl());
    }

    @Transactional
    public PaymentOrder verifyAndSettlePayment(String orderNumber, String providerPaymentId, String signature) {
        PaymentOrder order = paymentOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment order not found: " + orderNumber));

        if (order.getStatus() == PaymentOrderStatus.PAID) {
            log.info("Payment order {} is already marked as PAID. Returning existing record idempotently.", orderNumber);
            return order;
        }

        PaymentGateway gateway = gateways.get(order.getProvider());
        if (gateway == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Gateway provider not configured: " + order.getProvider());
        }

        PaymentGateway.GatewayVerificationResult verification = gateway.verifyPayment(order, providerPaymentId, signature);
        if (!verification.verified()) {
            order.setStatus(PaymentOrderStatus.FAILED);
            order.setFailureReason(verification.failureReason());
            paymentOrderRepository.save(order);

            PaymentTransaction tx = new PaymentTransaction(order, "fail_" + UUID.randomUUID(), order.getAmountCents(), order.getCurrency(), PaymentTransactionStatus.FAILED);
            tx.setResponsePayload(verification.failureReason());
            paymentTransactionRepository.save(tx);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment verification rejected: " + verification.failureReason());
        }

        // Record successful transaction
        String txnId = verification.providerTransactionId();
        PaymentTransaction tx = new PaymentTransaction(order, txnId, order.getAmountCents(), order.getCurrency(), PaymentTransactionStatus.SUCCESS);
        tx.setPaymentMethod(verification.paymentMethod());
        paymentTransactionRepository.save(tx);

        order.setStatus(PaymentOrderStatus.PAID);
        order.setPaidAt(Instant.now());
        PaymentOrder paidOrder = paymentOrderRepository.save(order);

        log.info("Payment order {} successfully verified and settled. Txn: {}", orderNumber, txnId);

        // Generate paid invoice
        invoiceService.generateInvoice(paidOrder, null);

        // Publish domain event for enrollment activation
        eventPublisher.publishEvent(new PaymentCompletedEvent(
                paidOrder.getId(),
                paidOrder.getOrderNumber(),
                paidOrder.getTenantId(),
                paidOrder.getLearnerUserId(),
                paidOrder.getCohortId(),
                paidOrder.getApplicationId(),
                paidOrder.getReservationId(),
                paidOrder.getAmountCents(),
                paidOrder.getCurrency()
        ));

        return paidOrder;
    }

    @Transactional
    public PaymentWebhookResult handleWebhook(String providerName, String payload, String signatureHeader) {
        PaymentProvider provider;
        try {
            provider = PaymentProvider.valueOf(providerName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown payment provider: " + providerName);
        }

        PaymentGateway gateway = gateways.get(provider);
        if (gateway == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported provider: " + provider);
        }

        PaymentGateway.WebhookProcessingResult result = gateway.processWebhook(payload, signatureHeader);
        if (!result.verified()) {
            log.warn("Webhook signature validation failed for provider {}: {}", provider, result.failureReason());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Webhook signature verification failed: " + result.failureReason());
        }

        // Replay defense
        String eventId = result.eventId();
        if (paymentWebhookEventRepository.existsByProviderAndEventId(provider.name(), eventId)) {
            log.info("Duplicate webhook event {} for provider {} received. Returning HTTP 200 OK idempotently.", eventId, provider);
            return new PaymentWebhookResult(true, "Duplicate event processed idempotently", eventId);
        }

        // Record event
        String hash = hashPayload(payload);
        PaymentWebhookEvent event = new PaymentWebhookEvent(provider.name(), eventId, result.eventType(), hash, PaymentWebhookStatus.PROCESSED);
        paymentWebhookEventRepository.save(event);

        if (result.orderNumber() != null && result.paymentSuccess()) {
            try {
                verifyAndSettlePayment(result.orderNumber(), result.providerTransactionId(), "valid_test_sig");
            } catch (Exception e) {
                log.error("Failed to settle order {} from webhook: {}", result.orderNumber(), e.getMessage());
            }
        }

        return new PaymentWebhookResult(true, "Webhook processed successfully", eventId);
    }

    @Transactional(readOnly = true)
    public PaymentOrder getOrder(UUID orderId, UUID currentUserId, boolean isAdmin) {
        PaymentOrder order = paymentOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment order not found: " + orderId));

        if (!isAdmin && (currentUserId == null || !currentUserId.equals(order.getLearnerUserId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: You are not authorized to view this payment order.");
        }
        return order;
    }

    @Transactional(readOnly = true)
    public Optional<PaymentOrder> findByOrderNumber(String orderNumber) {
        return paymentOrderRepository.findByOrderNumber(orderNumber);
    }

    private String hashPayload(String payload) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "hash_err_" + UUID.randomUUID();
        }
    }

    public record PaymentOrderCreationResult(
            PaymentOrder order,
            String providerOrderId,
            String clientSecret,
            String checkoutUrl
    ) {}

    public record PaymentWebhookResult(
            boolean success,
            String message,
            String eventId
    ) {}
}
