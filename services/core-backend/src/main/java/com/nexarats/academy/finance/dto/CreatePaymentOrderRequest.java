package com.nexarats.academy.finance.dto;

import com.nexarats.academy.finance.model.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreatePaymentOrderRequest(
        UUID applicationId,
        UUID offerId,
        UUID cohortId,
        UUID reservationId,
        String currency,
        PaymentProvider provider,
        String idempotencyKey
) {}
