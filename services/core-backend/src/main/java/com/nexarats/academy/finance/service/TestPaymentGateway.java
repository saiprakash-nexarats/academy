package com.nexarats.academy.finance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexarats.academy.finance.model.PaymentOrder;
import com.nexarats.academy.finance.model.PaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class TestPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(TestPaymentGateway.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final org.springframework.core.env.Environment environment;

    public TestPaymentGateway(org.springframework.core.env.Environment environment) {
        this.environment = environment;
    }

    private void assertNotProduction() {
        if (environment != null) {
            String[] activeProfiles = environment.getActiveProfiles();
            for (String profile : activeProfiles) {
                if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                    throw new IllegalStateException("TestPaymentGateway is strictly disabled in production environments.");
                }
            }
        }
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.TEST_GATEWAY;
    }

    @Override
    public GatewayOrderResult createOrder(PaymentOrder order, String customerEmail, String customerName) {
        assertNotProduction();
        String providerOrderId = "test_ord_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String clientSecret = "test_sec_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        String checkoutUrl = "https://checkout.test.nexarats.academy/pay/" + providerOrderId;

        log.info("TestPaymentGateway: Created order {} for amount {} {}", providerOrderId, order.getAmountCents(), order.getCurrency());
        return new GatewayOrderResult(providerOrderId, clientSecret, checkoutUrl, Map.of("mode", "test"));
    }

    @Override
    public GatewayVerificationResult verifyPayment(PaymentOrder order, String providerPaymentId, String signature) {
        assertNotProduction();
        if (signature == null || (!signature.equals("test_valid_sig") && !signature.startsWith("valid_"))) {
            log.warn("TestPaymentGateway: Invalid payment verification signature for order {}", order.getOrderNumber());
            return new GatewayVerificationResult(false, null, null, "Invalid test payment verification signature.");
        }

        String txnId = (providerPaymentId != null && !providerPaymentId.isBlank())
                ? providerPaymentId
                : "test_txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        log.info("TestPaymentGateway: Verified payment for order {} with transaction {}", order.getOrderNumber(), txnId);
        return new GatewayVerificationResult(true, txnId, "TEST_CARD", null);
    }

    @Override
    public WebhookProcessingResult processWebhook(String payload, String signatureHeader) {
        assertNotProduction();
        if (signatureHeader == null || (!signatureHeader.equals("test_webhook_sig") && !signatureHeader.equals("valid_test_sig") && !signatureHeader.startsWith("sha256="))) {
            log.warn("TestPaymentGateway: Webhook rejected due to invalid signature header: {}", signatureHeader);
            return new WebhookProcessingResult(false, null, null, null, false, null, "Invalid webhook signature header.");
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventId = root.has("id") ? root.get("id").asText() : "evt_" + UUID.randomUUID();
            String eventType = root.has("type") ? root.get("type").asText() : "payment.succeeded";
            String orderNumber = root.has("orderNumber") ? root.get("orderNumber").asText() : null;
            boolean success = "payment.succeeded".equalsIgnoreCase(eventType) || "payment_intent.succeeded".equalsIgnoreCase(eventType);
            String txnId = root.has("transactionId") ? root.get("transactionId").asText() : "test_txn_" + UUID.randomUUID();

            return new WebhookProcessingResult(true, eventId, eventType, orderNumber, success, txnId, null);
        } catch (Exception e) {
            log.error("TestPaymentGateway: Failed to parse webhook payload", e);
            return new WebhookProcessingResult(false, null, null, null, false, null, "Malformed webhook JSON payload: " + e.getMessage());
        }
    }
}
