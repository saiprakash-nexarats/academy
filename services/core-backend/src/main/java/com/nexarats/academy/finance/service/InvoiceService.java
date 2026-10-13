package com.nexarats.academy.finance.service;

import com.nexarats.academy.finance.model.Invoice;
import com.nexarats.academy.finance.model.InvoiceStatus;
import com.nexarats.academy.finance.model.PaymentOrder;
import com.nexarats.academy.finance.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public Invoice generateInvoice(PaymentOrder order, UUID enrollmentId) {
        Optional<Invoice> existing = invoiceRepository.findByPaymentOrderId(order.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        String invNumber = "INV-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Invoice invoice = new Invoice(
                invNumber,
                order.getLearnerUserId(),
                order,
                order.getAmountCents(),
                order.getCurrency()
        );
        invoice.setTenantId(order.getTenantId());
        invoice.setEnrollmentId(enrollmentId);
        invoice.setStatus(InvoiceStatus.PAID);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Generated invoice {} for payment order {}", invNumber, order.getOrderNumber());
        return saved;
    }

    @Transactional(readOnly = true)
    public Invoice getInvoice(UUID id, UUID currentUserId, boolean isAdmin) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found: " + id));

        if (!isAdmin && (currentUserId == null || !currentUserId.equals(invoice.getLearnerUserId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: You are not authorized to view this invoice.");
        }
        return invoice;
    }

    @Transactional(readOnly = true)
    public List<Invoice> getMyInvoices(UUID learnerUserId) {
        if (learnerUserId == null) return List.of();
        return invoiceRepository.findByLearnerUserId(learnerUserId);
    }

    @Transactional(readOnly = true)
    public List<Invoice> getTenantInvoices(UUID tenantId) {
        if (tenantId == null) return invoiceRepository.findAll();
        return invoiceRepository.findByTenantId(tenantId);
    }
}
