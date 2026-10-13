package com.nexarats.academy.finance.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyPaymentRequest(
        @NotBlank(message = "Order number is required")
        String orderNumber,

        String providerPaymentId,

        @NotBlank(message = "Payment signature is required")
        String signature
) {}
