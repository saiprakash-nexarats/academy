package com.nexarats.academy.finance.service;

import com.nexarats.academy.finance.model.PaymentOrder;
import com.nexarats.academy.finance.model.PaymentProvider;

import java.util.Map;

public interface PaymentGateway {

    PaymentProvider getProvider();

    GatewayOrderResult createOrder(PaymentOrder order, String customerEmail, String customerName);

    GatewayVerificationResult verifyPayment(PaymentOrder order, String providerPaymentId, String signature);

    WebhookProcessingResult processWebhook(String payload, String signatureHeader);

    record GatewayOrderResult(
            String providerOrderId,
            String clientSecret,
            String checkoutUrl,
            Map<String, Object> metadata
    ) {}

    record GatewayVerificationResult(
            boolean verified,
            String providerTransactionId,
            String paymentMethod,
            String failureReason
    ) {}

    record WebhookProcessingResult(
            boolean verified,
            String eventId,
            String eventType,
            String orderNumber,
            boolean paymentSuccess,
            String providerTransactionId,
            String failureReason
    ) {}
}
