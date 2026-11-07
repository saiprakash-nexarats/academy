package com.nexarats.academy.onboarding.service;

import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.admissions.repository.EnrollmentRepository;
import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.repository.UserRepository;
import com.nexarats.academy.onboarding.dto.CompleteOnboardingResponse;
import com.nexarats.academy.onboarding.dto.OnboardingStatusResponse;
import com.nexarats.academy.onboarding.dto.StudentProfileDto;
import com.nexarats.academy.onboarding.model.AvatarAsset;
import com.nexarats.academy.onboarding.model.OnboardingStatus;
import com.nexarats.academy.onboarding.model.ScanStatus;
import com.nexarats.academy.onboarding.model.StudentProfile;
import com.nexarats.academy.onboarding.repository.AvatarAssetRepository;
import com.nexarats.academy.onboarding.repository.StudentProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StudentOnboardingService {

    private static final Logger log = LoggerFactory.getLogger(StudentOnboardingService.class);

    private final StudentProfileRepository profileRepository;
    private final AvatarAssetRepository avatarAssetRepository;
    private final LegalAgreementService legalAgreementService;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final StudentProfileService profileService;

    public StudentOnboardingService(
            StudentProfileRepository profileRepository,
            AvatarAssetRepository avatarAssetRepository,
            LegalAgreementService legalAgreementService,
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            StudentProfileService profileService) {
        this.profileRepository = profileRepository;
        this.avatarAssetRepository = avatarAssetRepository;
        this.legalAgreementService = legalAgreementService;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(UUID tenantId, UUID userId) {
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant context is required.");
        }

        StudentProfile profile = profileRepository.findByTenantIdAndUserId(tenantId, userId).orElse(null);

        boolean profileCompleted = profile != null
                && profile.getHeadline() != null && !profile.getHeadline().isBlank()
                && profile.getBio() != null && !profile.getBio().isBlank();

        boolean developerLinksCompleted = profile != null
                && ((profile.getGithubUrl() != null && !profile.getGithubUrl().isBlank())
                || (profile.getLinkedinUrl() != null && !profile.getLinkedinUrl().isBlank()));

        boolean avatarUploaded = false;
        if (profile != null && profile.getAvatarAssetId() != null) {
            Optional<AvatarAsset> assetOpt = avatarAssetRepository.findById(profile.getAvatarAssetId());
            if (assetOpt.isPresent() && assetOpt.get().getScanStatus() == ScanStatus.CLEAN) {
                avatarUploaded = true;
            }
        }

        List<String> pendingAgreements = new ArrayList<>();
        boolean legalAgreementsAccepted = legalAgreementService.areAllMandatoryAgreementsAccepted(
                tenantId, userId, pendingAgreements);

        boolean canActivate = profileCompleted && developerLinksCompleted && avatarUploaded && legalAgreementsAccepted;

        OnboardingStatus overallStatus = OnboardingStatus.NOT_STARTED;
        if (profile != null && profile.getOnboardingStatus() == OnboardingStatus.COMPLETED) {
            overallStatus = OnboardingStatus.COMPLETED;
        } else if (profile != null && (profile.getOnboardingStatus() == OnboardingStatus.IN_PROGRESS
                || profileCompleted || developerLinksCompleted || avatarUploaded || !pendingAgreements.isEmpty())) {
            overallStatus = OnboardingStatus.IN_PROGRESS;
        }

        // Strict tenant-scoped enrollment lookup (null-tenant records are strictly rejected)
        UUID activeEnrollmentId = null;
        EnrollmentStatus activeEnrollmentStatus = null;
        List<Enrollment> enrollments = enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(userId);
        for (Enrollment e : enrollments) {
            if (e.getTenantId() != null && tenantId.equals(e.getTenantId())) {
                activeEnrollmentId = e.getId();
                activeEnrollmentStatus = e.getStatus();
                break;
            }
        }

        String avatarUrl = profile != null ? profileService.resolveAvatarUrl(profile.getAvatarAssetId()) : null;
        StudentProfileDto profileDto = StudentProfileDto.fromEntity(profile, avatarUrl);

        return new OnboardingStatusResponse(
                overallStatus,
                profileCompleted,
                developerLinksCompleted,
                avatarUploaded,
                legalAgreementsAccepted,
                pendingAgreements,
                canActivate,
                profileDto,
                activeEnrollmentId,
                activeEnrollmentStatus
        );
    }

    @Transactional
    public CompleteOnboardingResponse completeOnboarding(UUID tenantId, UUID userId) {
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant context is required.");
        }

        StudentProfile profile = profileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student profile has not been initialized."));

        // 1. Strict Tenant Enrollment Prerequisite Verification
        List<Enrollment> allEnrollments = enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(userId);
        List<Enrollment> tenantEnrollments = allEnrollments.stream()
                .filter(e -> e.getTenantId() != null && tenantId.equals(e.getTenantId()))
                .toList();

        if (tenantEnrollments.isEmpty()) {
            log.warn("Onboarding activation rejected for user {}: No enrollment found in tenant {}", userId, tenantId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No enrollment found for student in the current tenant. An eligible enrollment is required before completing onboarding.");
        }

        // 2. Check Idempotency for already-completed students
        if (profile.getOnboardingStatus() == OnboardingStatus.COMPLETED) {
            Optional<Enrollment> activeEnr = tenantEnrollments.stream()
                    .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                    .findFirst();
            if (activeEnr.isPresent()) {
                log.info("Student onboarding already completed (idempotent replay) for user {} tenant {}", userId, tenantId);
                return new CompleteOnboardingResponse(
                        true,
                        "Student academic onboarding is already completed.",
                        OnboardingStatus.COMPLETED,
                        profile.getOnboardingCompletedAt(),
                        EnrollmentStatus.ACTIVE
                );
            }
        }

        // 3. Validate eligible enrollment status (must have at least one in ENROLLED status)
        List<Enrollment> eligibleEnrollments = tenantEnrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
                .toList();

        if (eligibleEnrollments.isEmpty()) {
            Enrollment current = tenantEnrollments.get(0);
            log.warn("Onboarding activation rejected for user {}: Ineligible enrollment status {}", userId, current.getStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Student has no eligible enrollment in ENROLLED status for activation. Current status: " + current.getStatus());
        }

        // 4. Validate all 4 completeness gates
        List<String> errors = new ArrayList<>();

        if (profile.getHeadline() == null || profile.getHeadline().isBlank()) {
            errors.add("Student headline is required");
        }
        if (profile.getBio() == null || profile.getBio().isBlank()) {
            errors.add("Student bio is required");
        }
        boolean hasDevLink = (profile.getGithubUrl() != null && !profile.getGithubUrl().isBlank())
                || (profile.getLinkedinUrl() != null && !profile.getLinkedinUrl().isBlank());
        if (!hasDevLink) {
            errors.add("At least one developer link (GitHub or LinkedIn) is required");
        }

        if (profile.getAvatarAssetId() == null) {
            errors.add("Student profile avatar is required");
        } else {
            AvatarAsset asset = avatarAssetRepository.findById(profile.getAvatarAssetId()).orElse(null);
            if (asset == null || asset.getScanStatus() != ScanStatus.CLEAN) {
                errors.add("A clean, verified avatar asset is required");
            }
        }

        List<String> missingAgreements = new ArrayList<>();
        boolean agreementsAccepted = legalAgreementService.areAllMandatoryAgreementsAccepted(
                tenantId, userId, missingAgreements);
        if (!agreementsAccepted) {
            errors.add("All mandatory legal agreements must be accepted: " + String.join(", ", missingAgreements));
        }

        if (!errors.isEmpty()) {
            log.warn("Onboarding activation rejected for user {}: {}", userId, errors);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Onboarding requirements incomplete: " + String.join("; ", errors));
        }

        // 5. Update profile status to COMPLETED
        Instant completedAt = Instant.now();
        profile.setOnboardingStatus(OnboardingStatus.COMPLETED);
        profile.setOnboardingCompletedAt(completedAt);
        profileRepository.save(profile);

        // 6. Transition all authorized tenant enrollments from ENROLLED to ACTIVE
        EnrollmentStatus resultingStatus = EnrollmentStatus.ACTIVE;
        for (Enrollment e : eligibleEnrollments) {
            e.setStatus(EnrollmentStatus.ACTIVE);
            enrollmentRepository.save(e);
            log.info("Transitioned enrollment {} to ACTIVE upon onboarding completion for user {} in tenant {}",
                    e.getId(), userId, tenantId);
        }

        // 7. Synchronize avatarUrl to User entity
        if (profile.getAvatarAssetId() != null) {
            String avatarUrl = profileService.resolveAvatarUrl(profile.getAvatarAssetId());
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setAvatarUrl(avatarUrl);
                userRepository.save(user);
                log.info("Synchronized avatarUrl {} to User entity for user {}", avatarUrl, userId);
            }
        }

        log.info("Student onboarding completed successfully for user {} tenant {}", userId, tenantId);

        return new CompleteOnboardingResponse(
                true,
                "Student academic onboarding completed successfully. Welcome to Nexarats Academy!",
                OnboardingStatus.COMPLETED,
                completedAt,
                resultingStatus
        );
    }
}
