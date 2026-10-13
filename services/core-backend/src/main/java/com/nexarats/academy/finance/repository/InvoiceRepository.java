package com.nexarats.academy.finance.repository;

import com.nexarats.academy.finance.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByPaymentOrderId(UUID paymentOrderId);

    List<Invoice> findByLearnerUserId(UUID learnerUserId);

    List<Invoice> findByTenantId(UUID tenantId);
}
