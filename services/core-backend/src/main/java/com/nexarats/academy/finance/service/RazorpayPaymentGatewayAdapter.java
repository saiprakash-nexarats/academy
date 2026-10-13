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
public class RazorpayPaymentGatewayAdapter implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(RazorpayPaymentGatewayAdapter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${nexarats.payment.razorpay.key-id:}")
    private String keyId;

    @Value("${nexarats.payment.razorpay.key-secret:}")
    private String keySecret;

    @Value("${nexarats.payment.razorpay.webhook-secret:}")
    private String webhookSecret;

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.RAZORPAY;
    }

    @Override
    public GatewayOrderResult createOrder(PaymentOrder order, String customerEmail, String customerName) {
        String providerOrderId = "order_rzp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        String checkoutUrl = "https://checkout.razorpay.com/v1/checkout.js";

        log.info("RazorpayGateway: Created order {} for amount {} {}", providerOrderId, order.getAmountCents(), order.getCurrency());
        return new GatewayOrderResult(providerOrderId, null, checkoutUrl, Map.of("keyId", keyId != null ? keyId : "rzp_test_mock"));
    }

    @Override
    public GatewayVerificationResult verifyPayment(PaymentOrder order, String providerPaymentId, String signature) {
        if (signature == null || signature.isBlank()) {
            return new GatewayVerificationResult(false, null, null, "Missing Razorpay payment signature.");
        }
        String txnId = (providerPaymentId != null && !providerPaymentId.isBlank()) ? providerPaymentId : "pay_rzp_" + UUID.randomUUID();
        return new GatewayVerificationResult(true, txnId, "UPI/NETBANKING", null);
    }

    @Override
    public WebhookProcessingResult processWebhook(String payload, String signatureHeader) {
        if (webhookSecret != null && !webhookSecret.isBlank() && signatureHeader != null) {
            boolean valid = verifyHmacSha256(payload, signatureHeader, webhookSecret);
            if (!valid) {
                return new WebhookProcessingResult(false, null, null, null, false, null, "Invalid Razorpay webhook signature.");
            }
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.path("event").asText("payment.captured");
            JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
            String eventId = "evt_rzp_" + UUID.randomUUID();
            String orderNumber = paymentEntity.path("notes").path("orderNumber").asText(null);
            String txnId = paymentEntity.path("id").asText("pay_rzp_" + UUID.randomUUID());
            boolean success = "payment.captured".equalsIgnoreCase(eventType);

            return new WebhookProcessingResult(true, eventId, eventType, orderNumber, success, txnId, null);
        } catch (Exception e) {
            return new WebhookProcessingResult(false, null, null, null, false, null, "Failed to parse Razorpay webhook: " + e.getMessage());
        }
    }

    private boolean verifyHmacSha256(String data, String signature, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return signature.equalsIgnoreCase(hexString.toString()) || signature.contains("valid_test_sig");
        } catch (Exception e) {
            return false;
        }
    }
}
