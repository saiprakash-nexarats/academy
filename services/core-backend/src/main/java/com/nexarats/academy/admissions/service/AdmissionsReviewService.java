package com.nexarats.academy.admissions.service;

import com.nexarats.academy.admissions.model.*;
import com.nexarats.academy.admissions.repository.AdmissionOfferRepository;
import com.nexarats.academy.admissions.repository.ApplicationRepository;
import com.nexarats.academy.admissions.repository.ApplicationStatusHistoryRepository;
import com.nexarats.academy.cohort.model.Cohort;
import com.nexarats.academy.cohort.model.SeatReservation;
import com.nexarats.academy.cohort.service.CohortSeatService;
import com.nexarats.academy.finance.model.TuitionPrice;
import com.nexarats.academy.finance.model.TuitionTier;
import com.nexarats.academy.finance.service.PricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdmissionsReviewService {

    private static final Logger log = LoggerFactory.getLogger(AdmissionsReviewService.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final AdmissionOfferRepository admissionOfferRepository;
    private final CohortSeatService cohortSeatService;
    private final PricingService pricingService;

    public AdmissionsReviewService(ApplicationRepository applicationRepository,
                                   ApplicationStatusHistoryRepository statusHistoryRepository,
                                   AdmissionOfferRepository admissionOfferRepository,
                                   CohortSeatService cohortSeatService,
                                   PricingService pricingService) {
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.admissionOfferRepository = admissionOfferRepository;
        this.cohortSeatService = cohortSeatService;
        this.pricingService = pricingService;
    }

    @Transactional
    public ReviewResult reviewApplication(
            UUID applicationId,
            String decision,
            String reviewNotes,
            String rejectionReason,
            UUID targetCohortId,
            Long customTuitionCents,
            String currency,
            UUID reviewerUserId) {

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found: " + applicationId));

        if (app.getStatus() != ApplicationStatus.SUBMITTED && app.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot review application in status: " + app.getStatus());
        }

        ApplicationStatus fromStatus = app.getStatus();
        app.setReviewedByUserId(reviewerUserId);
        app.setReviewedAt(Instant.now());
        app.setReviewDecision(decision);

        if ("APPROVED".equalsIgnoreCase(decision)) {
            app.setStatus(ApplicationStatus.APPROVED);
            applicationRepository.save(app);

            // Determine cohort
            UUID cohortId = (targetCohortId != null) ? targetCohortId : app.getCohortId();
            if (cohortId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid cohort must be assigned to approve application.");
            }
            Cohort cohort = cohortSeatService.getCohort(cohortId);

            // Determine authoritative tuition price
            String effectiveCurrency = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "USD";
            long amountCents;
            UUID tuitionPriceId = null;

            if (customTuitionCents != null && customTuitionCents >= 0) {
                amountCents = customTuitionCents;
            } else {
                TuitionPrice price = pricingService.getTuitionPrice(cohort.getProgramId(), cohort.getCourseId(), effectiveCurrency, TuitionTier.STANDARD);
                amountCents = price.getAmountCents();
                tuitionPriceId = price.getId();
            }

            // Create admission offer valid for 7 days
            String offerNumber = "OFFER-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            Instant validUntil = Instant.now().plus(7, ChronoUnit.DAYS);

            AdmissionOffer offer = new AdmissionOffer(offerNumber, app, cohortId, amountCents, effectiveCurrency, validUntil);
            offer.setTenantId(app.getTenantId());
            offer.setTuitionPriceId(tuitionPriceId);
            offer.setIssuedByUserId(reviewerUserId);

            AdmissionOffer savedOffer = admissionOfferRepository.save(offer);

            // Reserve a seat in cohort for this offer (15 days validity or offer expiry)
            SeatReservation reservation = cohortSeatService.reserveSeat(
                    cohortId,
                    app.getApplicantUserId(),
                    app.getId(),
                    savedOffer.getId(),
                    7 * 24 * 60 // 7 days in minutes
            );

            // Record status history
            ApplicationStatusHistory history = new ApplicationStatusHistory(
                    app.getId(),
                    fromStatus,
                    ApplicationStatus.APPROVED,
                    reviewerUserId,
                    reviewNotes != null ? reviewNotes : "Application approved. Admission offer issued: " + offerNumber
            );
            statusHistoryRepository.save(history);

            log.info("Application {} approved by reviewer {}. Offer: {}, Reservation: {}", app.getApplicationNumber(), reviewerUserId, offerNumber, reservation.getReservationToken());
            return new ReviewResult(app, savedOffer, reservation);

        } else if ("REJECTED".equalsIgnoreCase(decision)) {
            app.setStatus(ApplicationStatus.REJECTED);
            app.setRejectionReason(rejectionReason != null ? rejectionReason : "Application did not meet admission criteria.");
            applicationRepository.save(app);

            ApplicationStatusHistory history = new ApplicationStatusHistory(
                    app.getId(),
                    fromStatus,
                    ApplicationStatus.REJECTED,
                    reviewerUserId,
                    rejectionReason != null ? rejectionReason : "Application rejected."
            );
            statusHistoryRepository.save(history);

            log.info("Application {} rejected by reviewer {}", app.getApplicationNumber(), reviewerUserId);
            return new ReviewResult(app, null, null);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid decision: " + decision + ". Allowed values: APPROVED, REJECTED");
        }
    }

    @Transactional
    public AdmissionOffer acceptOffer(UUID offerId, UUID currentUserId) {
        AdmissionOffer offer = admissionOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admission offer not found: " + offerId));

        Application app = offer.getApplication();
        if (currentUserId == null || !currentUserId.equals(app.getApplicantUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: You can only accept your own admission offer.");
        }

        if (offer.getStatus() != AdmissionOfferStatus.ISSUED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offer cannot be accepted in status: " + offer.getStatus());
        }

        if (Instant.now().isAfter(offer.getValidUntil())) {
            offer.setStatus(AdmissionOfferStatus.EXPIRED);
            admissionOfferRepository.save(offer);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admission offer has expired.");
        }

        offer.setStatus(AdmissionOfferStatus.ACCEPTED);
        offer.setRespondedAt(Instant.now());
        AdmissionOffer updated = admissionOfferRepository.save(offer);

        log.info("Admission offer {} accepted by user {}", offer.getOfferNumber(), currentUserId);
        return updated;
    }

    @Transactional(readOnly = true)
    public Optional<AdmissionOffer> getOfferByApplicationId(UUID applicationId) {
        return admissionOfferRepository.findByApplicationId(applicationId);
    }

    public record ReviewResult(
            Application application,
            AdmissionOffer offer,
            SeatReservation reservation
    ) {}
}
