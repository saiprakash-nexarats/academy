package com.nexarats.academy.finance.dto;

import com.nexarats.academy.finance.model.InvoiceStatus;
import java.time.Instant;
import java.util.UUID;

public record InvoiceDto(
        UUID id,
        String invoiceNumber,
        UUID learnerUserId,
        UUID paymentOrderId,
        UUID enrollmentId,
        long subtotalCents,
        long taxCents,
        long totalCents,
        String currency,
        InvoiceStatus status,
        Instant issuedAt,
        Instant paidAt,
        String pdfReceiptUrl
) {}
