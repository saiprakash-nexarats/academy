package com.nexarats.academy.finance.repository;

import com.nexarats.academy.finance.model.PaymentOrder;
import com.nexarats.academy.finance.model.PaymentOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, UUID> {

    Optional<PaymentOrder> findByOrderNumber(String orderNumber);

    Optional<PaymentOrder> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentOrder> findByProviderOrderId(String providerOrderId);

    List<PaymentOrder> findByLearnerUserId(UUID learnerUserId);

    List<PaymentOrder> findByLearnerUserIdAndStatus(UUID learnerUserId, PaymentOrderStatus status);
}
