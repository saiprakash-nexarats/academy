package com.nexarats.academy.finance.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 64)
    private String invoiceNumber;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "learner_user_id", nullable = false)
    private UUID learnerUserId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_order_id", nullable = false, unique = true)
    private PaymentOrder paymentOrder;

    @Column(name = "enrollment_id")
    private UUID enrollmentId;

    @Column(name = "subtotal_cents", nullable = false)
    private long subtotalCents;

    @Column(name = "tax_cents", nullable = false)
    private long taxCents = 0;

    @Column(name = "total_cents", nullable = false)
    private long totalCents;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private InvoiceStatus status = InvoiceStatus.PAID;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt = Instant.now();

    @Column(name = "paid_at")
    private Instant paidAt = Instant.now();

    @Column(name = "pdf_receipt_url", length = 1024)
    private String pdfReceiptUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Invoice() {
    }

    public Invoice(String invoiceNumber, UUID learnerUserId, PaymentOrder paymentOrder, long totalCents, String currency) {
        this.invoiceNumber = invoiceNumber;
        this.learnerUserId = learnerUserId;
        this.paymentOrder = paymentOrder;
        this.subtotalCents = totalCents;
        this.taxCents = 0;
        this.totalCents = totalCents;
        this.currency = currency;
        this.status = InvoiceStatus.PAID;
        this.paidAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getLearnerUserId() {
        return learnerUserId;
    }

    public void setLearnerUserId(UUID learnerUserId) {
        this.learnerUserId = learnerUserId;
    }

    public PaymentOrder getPaymentOrder() {
        return paymentOrder;
    }

    public void setPaymentOrder(PaymentOrder paymentOrder) {
        this.paymentOrder = paymentOrder;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(UUID enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public long getSubtotalCents() {
        return subtotalCents;
    }

    public void setSubtotalCents(long subtotalCents) {
        this.subtotalCents = subtotalCents;
    }

    public long getTaxCents() {
        return taxCents;
    }

    public void setTaxCents(long taxCents) {
        this.taxCents = taxCents;
    }

    public long getTotalCents() {
        return totalCents;
    }

    public void setTotalCents(long totalCents) {
        this.totalCents = totalCents;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public String getPdfReceiptUrl() {
        return pdfReceiptUrl;
    }

    public void setPdfReceiptUrl(String pdfReceiptUrl) {
        this.pdfReceiptUrl = pdfReceiptUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
