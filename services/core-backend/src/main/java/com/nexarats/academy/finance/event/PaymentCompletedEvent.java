package com.nexarats.academy.finance.event;

import java.util.UUID;

public record PaymentCompletedEvent(
        UUID orderId,
        String orderNumber,
        UUID tenantId,
        UUID learnerUserId,
        UUID cohortId,
        UUID applicationId,
        UUID reservationId,
        long amountCents,
        String currency
) {}
