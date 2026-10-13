package com.nexarats.academy.admissions.web;

import com.nexarats.academy.admissions.dto.*;
import com.nexarats.academy.admissions.model.AdmissionOffer;
import com.nexarats.academy.admissions.model.Application;
import com.nexarats.academy.admissions.model.ApplicationStatus;
import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.cohort.model.SeatReservation;
import com.nexarats.academy.admissions.service.AdmissionsReviewService;
import com.nexarats.academy.admissions.service.ApplicationService;
import com.nexarats.academy.admissions.service.EnrollmentService;
import com.nexarats.academy.common.security.UserPrincipal;
import com.nexarats.academy.finance.dto.PaymentOrderDto;
import com.nexarats.academy.finance.model.PaymentOrder;
import com.nexarats.academy.finance.model.PaymentProvider;
import com.nexarats.academy.finance.service.PaymentOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enterprise Admissions and Enrollment REST Controller.
 * Provides application submission, review, offer acceptance, and enrollment visibility.
 */
@RestController
@RequestMapping("/api/v1/admissions")
public class AdmissionsController {

    private final ApplicationService applicationService;
    private final AdmissionsReviewService admissionsReviewService;
    private final EnrollmentService enrollmentService;
    private final PaymentOrderService paymentOrderService;

    public AdmissionsController(
            ApplicationService applicationService,
            AdmissionsReviewService admissionsReviewService,
            EnrollmentService enrollmentService,
            PaymentOrderService paymentOrderService
    ) {
        this.applicationService = applicationService;
        this.admissionsReviewService = admissionsReviewService;
        this.enrollmentService = enrollmentService;
        this.paymentOrderService = paymentOrderService;
    }

    @PostMapping("/applications")
    public ResponseEntity<ApplicationSummaryDto> submitApplication(
            @Valid @RequestBody SubmitApplicationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UUID authenticatedUserId = (principal != null) ? principal.getUserId() : null;
        UUID tenantId = (principal != null) ? principal.getTenantId() : null;

        Application app = applicationService.submitApplication(
                request.fullName(),
                request.email(),
                request.phone(),
                request.targetProgramOrCourse(),
                request.experienceLevel(),
                request.statementOfPurpose(),
                authenticatedUserId,
                tenantId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(app));
    }

    @GetMapping("/applications/mine")
    public ResponseEntity<List<ApplicationSummaryDto>> getMyApplications(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to view applications");
        }

        List<Application> apps = applicationService.getMyApplications(principal.getUserId());
        List<ApplicationSummaryDto> dtos = apps.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<ApplicationSummaryDto> getApplication(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to view application");
        }

        boolean isAdmin = checkIsStaffOrAdmin(principal);
        Application app = applicationService.getApplication(id, principal.getUserId(), isAdmin);
        return ResponseEntity.ok(toDto(app));
    }

    @PostMapping("/applications/{id}/withdraw")
    public ResponseEntity<ApplicationSummaryDto> withdrawApplication(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to withdraw application");
        }

        String reason = (body != null) ? body.get("reason") : "Withdrawn by applicant.";
        Application updated = applicationService.withdrawApplication(id, principal.getUserId(), reason);
        return ResponseEntity.ok(toDto(updated));
    }

    @GetMapping("/applications")
    public ResponseEntity<Page<ApplicationSummaryDto>> listApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) UUID courseId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null || !checkIsStaffOrAdmin(principal)) {
            throw new AccessDeniedException("Administrative privileges required to list all applications");
        }

        Page<Application> page = applicationService.listApplications(
                principal.getTenantId(), status, programId, courseId, pageable
        );
        Page<ApplicationSummaryDto> dtoPage = page.map(this::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping("/applications/{id}/review")
    public ResponseEntity<Map<String, Object>> reviewApplication(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewApplicationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null || !checkIsStaffOrAdmin(principal)) {
            throw new AccessDeniedException("Administrative privileges required to review applications");
        }

        AdmissionsReviewService.ReviewResult result = admissionsReviewService.reviewApplication(
                id,
                request.decision(),
                request.reviewNotes(),
                request.rejectionReason(),
                request.targetCohortId(),
                request.customTuitionCents(),
                request.currency(),
                principal.getUserId()
        );

        ApplicationSummaryDto appDto = toDto(result.application());
        AdmissionOfferDto offerDto = (result.offer() != null) ? toDto(result.offer()) : null;
        SeatReservationDto resDto = (result.reservation() != null) ? toDto(result.reservation()) : null;

        return ResponseEntity.ok(Map.of(
                "application", appDto,
                "offer", offerDto != null ? offerDto : Map.of(),
                "reservation", resDto != null ? resDto : Map.of()
        ));
    }

    @GetMapping("/applications/{id}/offer")
    public ResponseEntity<AdmissionOfferDto> getOffer(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to view admission offer");
        }

        boolean isAdmin = checkIsStaffOrAdmin(principal);
        Application app = applicationService.getApplication(id, principal.getUserId(), isAdmin);

        AdmissionOffer offer = admissionsReviewService.getOfferByApplicationId(app.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No admission offer found for application: " + id));

        return ResponseEntity.ok(toDto(offer));
    }

    @PostMapping("/offers/{id}/accept")
    public ResponseEntity<AdmissionOfferDto> acceptOffer(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to accept offer");
        }

        AdmissionOffer accepted = admissionsReviewService.acceptOffer(id, principal.getUserId());
        return ResponseEntity.ok(toDto(accepted));
    }

    @PostMapping("/offers/{id}/pay")
    public ResponseEntity<PaymentOrderDto> initiateOfferPayment(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to initiate payment");
        }

        AdmissionOffer offer = admissionsReviewService.getOfferByApplicationId(id)
                .or(() -> admissionsReviewService.getOfferByApplicationId(id)) // in case id is offer or app
                .orElse(null);

        // Fallback: search offer directly if needed
        AdmissionOffer effectiveOffer = offer;
        if (effectiveOffer == null) {
            // accept direct offerId
            effectiveOffer = admissionsReviewService.acceptOffer(id, principal.getUserId());
        }

        Application app = effectiveOffer.getApplication();
        if (!principal.getUserId().equals(app.getApplicantUserId())) {
            throw new AccessDeniedException("You can only pay for your own admission offer");
        }

        String providerStr = (body != null) ? body.get("provider") : "TEST_GATEWAY";
        PaymentProvider provider;
        try {
            provider = PaymentProvider.valueOf(providerStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            provider = PaymentProvider.TEST_GATEWAY;
        }

        String idempotencyKey = (body != null) ? body.get("idempotencyKey") : null;

        PaymentOrderService.PaymentOrderCreationResult result = paymentOrderService.createOrder(
                effectiveOffer.getTenantId(),
                principal.getUserId(),
                app.getId(),
                effectiveOffer.getId(),
                effectiveOffer.getCohortId(),
                null,
                effectiveOffer.getOfferedAmountCents(),
                effectiveOffer.getCurrency(),
                provider,
                idempotencyKey,
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

    @GetMapping("/enrollments/mine")
    public ResponseEntity<List<EnrollmentDto>> getMyEnrollments(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to view enrollments");
        }

        List<Enrollment> enrollments = enrollmentService.getMyEnrollments(principal.getUserId());
        List<EnrollmentDto> dtos = enrollments.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/enrollments")
    public ResponseEntity<Page<EnrollmentDto>> listEnrollments(
            @RequestParam(required = false) UUID cohortId,
            @RequestParam(required = false) com.nexarats.academy.admissions.model.EnrollmentStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null || !checkIsStaffOrAdmin(principal)) {
            throw new AccessDeniedException("Administrative privileges required to list all enrollments");
        }

        Page<Enrollment> page = enrollmentService.getEnrollments(principal.getTenantId(), cohortId, status, pageable);
        return ResponseEntity.ok(page.map(this::toDto));
    }

    private boolean checkIsStaffOrAdmin(UserPrincipal principal) {
        return principal.getAuthorities().stream().anyMatch(a -> {
            String auth = a.getAuthority();
            return auth.contains("ADMIN") || auth.contains("STAFF") || auth.contains("DIRECTOR") || auth.contains("INSTRUCTOR");
        });
    }

    private ApplicationSummaryDto toDto(Application app) {
        return new ApplicationSummaryDto(
                app.getId(),
                app.getApplicationNumber(),
                app.getFullName(),
                app.getEmail(),
                app.getPhone(),
                app.getProgramId(),
                app.getCourseId(),
                app.getCohortId(),
                app.getExperienceLevel(),
                app.getStatus(),
                app.getSubmittedAt()
        );
    }

    private AdmissionOfferDto toDto(AdmissionOffer offer) {
        return new AdmissionOfferDto(
                offer.getId(),
                offer.getOfferNumber(),
                offer.getApplication().getId(),
                offer.getCohortId(),
                offer.getOfferedAmountCents(),
                offer.getCurrency(),
                offer.getStatus(),
                offer.getValidUntil(),
                offer.getIssuedAt()
        );
    }

    private EnrollmentDto toDto(Enrollment enr) {
        return new EnrollmentDto(
                enr.getId(),
                enr.getEnrollmentNumber(),
                enr.getLearnerUserId(),
                enr.getProgramId(),
                enr.getCourseId(),
                enr.getCohortId(),
                enr.getApplicationId(),
                enr.getStatus(),
                enr.getEnrolledAt()
        );
    }

    private SeatReservationDto toDto(SeatReservation res) {
        return new SeatReservationDto(
                res.getId(),
                res.getCohortId(),
                res.getReservationToken(),
                res.getStatus(),
                res.getExpiresAt()
        );
    }
}
