package com.nexarats.academy.finance.web;

import com.nexarats.academy.common.security.UserPrincipal;
import com.nexarats.academy.finance.dto.CreatePaymentOrderRequest;
import com.nexarats.academy.finance.dto.PaymentOrderDto;
import com.nexarats.academy.finance.dto.VerifyPaymentRequest;
import com.nexarats.academy.finance.model.PaymentOrder;
import com.nexarats.academy.finance.model.TuitionPrice;
import com.nexarats.academy.finance.model.TuitionTier;
import com.nexarats.academy.finance.service.PaymentOrderService;
import com.nexarats.academy.finance.service.PricingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Enterprise Financial and Payment REST Controller.
 * Provides payment order initialization, verification, and webhook processing.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentOrderService paymentOrderService;
    private final PricingService pricingService;

    public PaymentController(PaymentOrderService paymentOrderService, PricingService pricingService) {
        this.paymentOrderService = paymentOrderService;
        this.pricingService = pricingService;
    }

    @PostMapping("/orders")
    public ResponseEntity<PaymentOrderDto> createPaymentOrder(
            @Valid @RequestBody CreatePaymentOrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to initiate payment order");
        }

        String effectiveCurrency = (request.currency() != null && !request.currency().isBlank())
                ? request.currency().toUpperCase() : "USD";

        // Determine server-authoritative tuition amount
        TuitionPrice tuitionPrice = pricingService.getTuitionPrice(
                null, null, effectiveCurrency, TuitionTier.STANDARD
        );
        long amountCents = tuitionPrice.getAmountCents();

        PaymentOrderService.PaymentOrderCreationResult result = paymentOrderService.createOrder(
                principal.getTenantId(),
                principal.getUserId(),
                request.applicationId(),
                request.offerId(),
                request.cohortId(),
                request.reservationId(),
                amountCents,
                effectiveCurrency,
                request.provider(),
                request.idempotencyKey(),
                principal.getEmail(),
                principal.getName()
        );

        PaymentOrder order = result.order();
        PaymentOrderDto dto = new PaymentOrderDto(
                order.getId(),
                order.getOrderNumber(),
                order.getAmountCents(),
                order.getCurrency(),
                order.getProvider(),
                result.providerOrderId(),
                result.clientSecret(),
                result.checkoutUrl(),
                order.getStatus(),
                order.getIdempotencyKey(),
                order.getPaidAt()
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/orders/verify")
    public ResponseEntity<PaymentOrderDto> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to verify payment");
        }

        PaymentOrder settledOrder = paymentOrderService.verifyAndSettlePayment(
                request.orderNumber(),
                request.providerPaymentId(),
                request.signature()
        );

        PaymentOrderDto dto = new PaymentOrderDto(
                settledOrder.getId(),
                settledOrder.getOrderNumber(),
                settledOrder.getAmountCents(),
                settledOrder.getCurrency(),
                settledOrder.getProvider(),
                settledOrder.getProviderOrderId(),
                null,
                null,
                settledOrder.getStatus(),
                settledOrder.getIdempotencyKey(),
                settledOrder.getPaidAt()
        );

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<PaymentOrderDto> getPaymentOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to view payment order");
        }

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN") || a.getAuthority().contains("FINANCE"));

        PaymentOrder order = paymentOrderService.getOrder(id, principal.getUserId(), isAdmin);

        PaymentOrderDto dto = new PaymentOrderDto(
                order.getId(),
                order.getOrderNumber(),
                order.getAmountCents(),
                order.getCurrency(),
                order.getProvider(),
                order.getProviderOrderId(),
                null,
                null,
                order.getStatus(),
                order.getIdempotencyKey(),
                order.getPaidAt()
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/webhooks/{provider}")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @PathVariable String provider,
            @RequestBody String payload,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String genericSig,
            @RequestHeader(value = "Stripe-Signature", required = false) String stripeSig,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String razorpaySig
    ) {
        String effectiveSignature = genericSig;
        if ("stripe".equalsIgnoreCase(provider) && stripeSig != null) {
            effectiveSignature = stripeSig;
        } else if ("razorpay".equalsIgnoreCase(provider) && razorpaySig != null) {
            effectiveSignature = razorpaySig;
        }

        PaymentOrderService.PaymentWebhookResult result = paymentOrderService.handleWebhook(
                provider, payload, effectiveSignature
        );

        return ResponseEntity.ok(Map.of(
                "success", result.success(),
                "message", result.message(),
                "eventId", result.eventId() != null ? result.eventId() : ""
        ));
    }
}
