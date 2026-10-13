package com.nexarats.academy.finance.web;

import com.nexarats.academy.common.security.UserPrincipal;
import com.nexarats.academy.finance.dto.InvoiceDto;
import com.nexarats.academy.finance.model.Invoice;
import com.nexarats.academy.finance.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enterprise Billing & Tuition Invoice REST Controller.
 */
@RestController
@RequestMapping("/api/v1/billing/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/mine")
    public ResponseEntity<List<InvoiceDto>> getMyInvoices(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to view invoices");
        }

        List<Invoice> invoices = invoiceService.getMyInvoices(principal.getUserId());
        List<InvoiceDto> dtos = invoices.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoice(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to view invoice");
        }

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN") || a.getAuthority().contains("FINANCE"));

        Invoice invoice = invoiceService.getInvoice(id, principal.getUserId(), isAdmin);
        return ResponseEntity.ok(toDto(invoice));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceDto>> getTenantInvoices(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to list tenant invoices");
        }

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN") || a.getAuthority().contains("FINANCE"));

        if (!isAdmin) {
            throw new AccessDeniedException("Administrative privileges required to view all tenant invoices");
        }

        List<Invoice> invoices = invoiceService.getTenantInvoices(principal.getTenantId());
        List<InvoiceDto> dtos = invoices.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private InvoiceDto toDto(Invoice invoice) {
        return new InvoiceDto(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getLearnerUserId(),
                invoice.getPaymentOrder() != null ? invoice.getPaymentOrder().getId() : null,
                invoice.getEnrollmentId(),
                invoice.getSubtotalCents(),
                invoice.getTaxCents(),
                invoice.getTotalCents(),
                invoice.getCurrency(),
                invoice.getStatus(),
                invoice.getIssuedAt(),
                invoice.getPaidAt(),
                invoice.getPdfReceiptUrl()
        );
    }
}
