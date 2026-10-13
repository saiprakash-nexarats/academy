package com.nexarats.academy.finance.dto;

import com.nexarats.academy.finance.model.PaymentOrderStatus;
import com.nexarats.academy.finance.model.PaymentProvider;
import java.time.Instant;
import java.util.UUID;

public record PaymentOrderDto(
        UUID id,
        String orderNumber,
        long amountCents,
        String currency,
        PaymentProvider provider,
        String providerOrderId,
        String clientSecret,
        String checkoutUrl,
        PaymentOrderStatus status,
        String idempotencyKey,
        Instant paidAt
) {}
