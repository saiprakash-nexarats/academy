package com.nexarats.academy.onboarding;

import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.admissions.repository.EnrollmentRepository;
import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.repository.UserRepository;
import com.nexarats.academy.onboarding.dto.CompleteOnboardingResponse;
import com.nexarats.academy.onboarding.dto.OnboardingStatusResponse;
import com.nexarats.academy.onboarding.model.AvatarAsset;
import com.nexarats.academy.onboarding.model.OnboardingStatus;
import com.nexarats.academy.onboarding.model.ScanStatus;
import com.nexarats.academy.onboarding.model.StudentProfile;
import com.nexarats.academy.onboarding.repository.AvatarAssetRepository;
import com.nexarats.academy.onboarding.repository.StudentProfileRepository;
import com.nexarats.academy.onboarding.service.LegalAgreementService;
import com.nexarats.academy.onboarding.service.StudentOnboardingService;
import com.nexarats.academy.onboarding.service.StudentProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentOnboardingServiceTest {

    @Mock
    private StudentProfileRepository profileRepository;

    @Mock
    private AvatarAssetRepository avatarAssetRepository;

    @Mock
    private LegalAgreementService legalAgreementService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileService profileService;

    private StudentOnboardingService onboardingService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        onboardingService = new StudentOnboardingService(
                profileRepository,
                avatarAssetRepository,
                legalAgreementService,
                enrollmentRepository,
                userRepository,
                profileService
        );
    }

    @Test
    @DisplayName("Calculates NOT_STARTED status when no profile fields populated")
    void calculatesNotStartedStatus() {
        when(profileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.empty());
        when(enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(userId)).thenReturn(Collections.emptyList());

        OnboardingStatusResponse status = onboardingService.getOnboardingStatus(tenantId, userId);

        assertThat(status.overallStatus()).isEqualTo(OnboardingStatus.NOT_STARTED);
        assertThat(status.canActivate()).isFalse();
        assertThat(status.profileCompleted()).isFalse();
    }

    @Test
    @DisplayName("Calculates IN_PROGRESS status when profile partially completed")
    void calculatesInProgressStatus() {
        StudentProfile profile = new StudentProfile(tenantId, userId);
        profile.setHeadline("Software Engineer");
        profile.setOnboardingStatus(OnboardingStatus.IN_PROGRESS);

        when(profileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(userId)).thenReturn(Collections.emptyList());
        when(legalAgreementService.areAllMandatoryAgreementsAccepted(eq(tenantId), eq(userId), any())).thenReturn(false);

        OnboardingStatusResponse status = onboardingService.getOnboardingStatus(tenantId, userId);

        assertThat(status.overallStatus()).isEqualTo(OnboardingStatus.IN_PROGRESS);
        assertThat(status.canActivate()).isFalse();
    }

    @Test
    @DisplayName("Rejects onboarding activation if profile headline or bio missing")
    void rejectsActivationIfProfileIncomplete() {
        StudentProfile profile = new StudentProfile(tenantId, userId);
        // Headline and bio missing
        when(profileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> onboardingService.completeOnboarding(tenantId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Student headline is required");
    }

    @Test
    @DisplayName("Rejects onboarding activation if developer link is missing")
    void rejectsActivationIfDevLinkMissing() {
        StudentProfile profile = new StudentProfile(tenantId, userId);
        profile.setHeadline("Full Stack Developer");
        profile.setBio("Passionate learner");
        // No github or linkedin url
        when(profileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> onboardingService.completeOnboarding(tenantId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("At least one developer link");
    }

    @Test
    @DisplayName("Rejects onboarding activation if avatar is not uploaded")
    void rejectsActivationIfAvatarMissing() {
        StudentProfile profile = new StudentProfile(tenantId, userId);
        profile.setHeadline("Full Stack Developer");
        profile.setBio("Passionate learner");
        profile.setGithubUrl("https://github.com/testuser");
        profile.setAvatarAssetId(null);
        when(profileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> onboardingService.completeOnboarding(tenantId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Student profile avatar is required");
    }

    @Test
    @DisplayName("Rejects onboarding activation if legal agreements are not all accepted")
    void rejectsActivationIfAgreementsMissing() {
        UUID avatarId = UUID.randomUUID();
        StudentProfile profile = new StudentProfile(tenantId, userId);
        profile.setHeadline("Full Stack Developer");
        profile.setBio("Passionate learner");
        profile.setGithubUrl("https://github.com/testuser");
        profile.setAvatarAssetId(avatarId);

        AvatarAsset asset = new AvatarAsset();
        asset.setId(avatarId);
        asset.setScanStatus(ScanStatus.CLEAN);

        when(profileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(avatarAssetRepository.findById(avatarId)).thenReturn(Optional.of(asset));
        when(legalAgreementService.areAllMandatoryAgreementsAccepted(eq(tenantId), eq(userId), any())).thenReturn(false);

        assertThatThrownBy(() -> onboardingService.completeOnboarding(tenantId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("All mandatory legal agreements must be accepted");
    }

    @Test
    @DisplayName("Successfully activates onboarding and transitions enrollment from ENROLLED to ACTIVE")
    void completesOnboardingSuccessfully() {
        UUID avatarId = UUID.randomUUID();
        StudentProfile profile = new StudentProfile(tenantId, userId);
        profile.setHeadline("Full Stack Developer");
        profile.setBio("Passionate learner");
        profile.setGithubUrl("https://github.com/testuser");
        profile.setLinkedinUrl("https://linkedin.com/in/testuser");
        profile.setAvatarAssetId(avatarId);

        AvatarAsset asset = new AvatarAsset();
        asset.setId(avatarId);
        asset.setScanStatus(ScanStatus.CLEAN);

        when(profileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(avatarAssetRepository.findById(avatarId)).thenReturn(Optional.of(asset));
        when(legalAgreementService.areAllMandatoryAgreementsAccepted(eq(tenantId), eq(userId), any())).thenReturn(true);
        when(profileRepository.save(any(StudentProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        Enrollment enrollment = new Enrollment();
        enrollment.setId(UUID.randomUUID());
        enrollment.setTenantId(tenantId);
        enrollment.setLearnerUserId(userId);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        when(enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(userId)).thenReturn(List.of(enrollment));

        User user = new User("student@test.com", "John", "Doe");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profileService.resolveAvatarUrl(avatarId)).thenReturn("/api/v1/onboarding/avatar/" + avatarId);

        CompleteOnboardingResponse resp = onboardingService.completeOnboarding(tenantId, userId);

        assertThat(resp.success()).isTrue();
        assertThat(resp.onboardingStatus()).isEqualTo(OnboardingStatus.COMPLETED);
        assertThat(resp.enrollmentStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(profile.getOnboardingStatus()).isEqualTo(OnboardingStatus.COMPLETED);
        assertThat(profile.getOnboardingCompletedAt()).isNotNull();

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        verify(enrollmentRepository, times(1)).save(enrollment);
        verify(userRepository, times(1)).save(user);
        assertThat(user.getAvatarUrl()).isEqualTo("/api/v1/onboarding/avatar/" + avatarId);
    }
}
