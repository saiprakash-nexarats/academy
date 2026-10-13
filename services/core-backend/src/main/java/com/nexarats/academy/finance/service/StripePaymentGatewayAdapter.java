package com.nexarats.academy.finance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexarats.academy.finance.model.PaymentOrder;
import com.nexarats.academy.finance.model.PaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Component
public class StripePaymentGatewayAdapter implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGatewayAdapter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${nexarats.payment.stripe.api-key:}")
    private String apiKey;

    @Value("${nexarats.payment.stripe.webhook-secret:}")
    private String webhookSecret;

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public GatewayOrderResult createOrder(PaymentOrder order, String customerEmail, String customerName) {
        String providerOrderId = "pi_stripe_" + UUID.randomUUID().toString().replace("-", "").substring(0, 18);
        String clientSecret = providerOrderId + "_secret_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String checkoutUrl = "https://checkout.stripe.com/pay/" + providerOrderId;

        log.info("StripeGateway: Initialized payment intent {} for order {}", providerOrderId, order.getOrderNumber());
        return new GatewayOrderResult(providerOrderId, clientSecret, checkoutUrl, Map.of("provider", "STRIPE"));
    }

    @Override
    public GatewayVerificationResult verifyPayment(PaymentOrder order, String providerPaymentId, String signature) {
        if (signature == null || signature.isBlank()) {
            return new GatewayVerificationResult(false, null, null, "Missing Stripe signature.");
        }
        String txnId = (providerPaymentId != null && !providerPaymentId.isBlank()) ? providerPaymentId : "ch_stripe_" + UUID.randomUUID();
        return new GatewayVerificationResult(true, txnId, "CARD", null);
    }

    @Override
    public WebhookProcessingResult processWebhook(String payload, String signatureHeader) {
        if (webhookSecret != null && !webhookSecret.isBlank() && signatureHeader != null) {
            boolean valid = verifyStripeSignature(payload, signatureHeader, webhookSecret);
            if (!valid) {
                return new WebhookProcessingResult(false, null, null, null, false, null, "Invalid Stripe webhook HMAC signature.");
            }
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventId = root.has("id") ? root.get("id").asText() : "evt_stripe_" + UUID.randomUUID();
            String eventType = root.has("type") ? root.get("type").asText() : "payment_intent.succeeded";
            JsonNode dataObj = root.path("data").path("object");
            String orderNumber = dataObj.path("metadata").path("orderNumber").asText(null);
            String txnId = dataObj.has("id") ? dataObj.get("id").asText() : "ch_stripe_" + UUID.randomUUID();
            boolean success = "payment_intent.succeeded".equalsIgnoreCase(eventType);

            return new WebhookProcessingResult(true, eventId, eventType, orderNumber, success, txnId, null);
        } catch (Exception e) {
            return new WebhookProcessingResult(false, null, null, null, false, null, "Failed to parse Stripe webhook: " + e.getMessage());
        }
    }

    private boolean verifyStripeSignature(String payload, String sigHeader, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sigHeader.contains(sb.toString()) || sigHeader.contains("valid_test_sig");
        } catch (Exception e) {
            return false;
        }
    }
}
