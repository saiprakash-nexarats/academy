package com.nexarats.academy.admissions.service;

import com.nexarats.academy.finance.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AdmissionsNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(AdmissionsNotificationListener.class);

    private final EnrollmentService enrollmentService;

    public AdmissionsNotificationListener(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @EventListener
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received PaymentCompletedEvent for order {}, learner: {}, cohort: {}",
                event.orderNumber(), event.learnerUserId(), event.cohortId());

        if (event.cohortId() != null && event.learnerUserId() != null) {
            try {
                enrollmentService.confirmEnrollment(
                        event.learnerUserId(),
                        event.cohortId(),
                        event.applicationId(),
                        event.orderId(),
                        event.reservationId()
                );
            } catch (Exception e) {
                log.error("Failed to automatically activate enrollment for payment event {}: {}", event.orderNumber(), e.getMessage(), e);
            }
        }
    }
}
