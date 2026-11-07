package com.nexarats.academy.onboarding;

import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.common.security.UserPrincipal;
import com.nexarats.academy.onboarding.dto.*;
import com.nexarats.academy.onboarding.model.AvatarAsset;
import com.nexarats.academy.onboarding.model.LegalAgreementType;
import com.nexarats.academy.onboarding.model.OnboardingStatus;
import com.nexarats.academy.onboarding.model.ScanStatus;
import com.nexarats.academy.onboarding.repository.AvatarAssetRepository;
import com.nexarats.academy.onboarding.service.*;
import com.nexarats.academy.onboarding.web.OnboardingController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
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
class OnboardingControllerSecurityTest {

    @Mock
    private StudentOnboardingService onboardingService;

    @Mock
    private StudentProfileService profileService;

    @Mock
    private LegalAgreementService legalAgreementService;

    @Mock
    private AvatarValidationService avatarValidationService;

    @Mock
    private AvatarMalwareScanningService malwareScanningService;

    @Mock
    private AvatarStorageService storageService;

    @Mock
    private AvatarAssetRepository avatarAssetRepository;

    private OnboardingController controller;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new OnboardingController(
                onboardingService,
                profileService,
                legalAgreementService,
                avatarValidationService,
                malwareScanningService,
                storageService,
                avatarAssetRepository
        );
    }

    private UserPrincipal createPrincipal(UUID tId, UUID uId, String role) {
        return new UserPrincipal(
                uId,
                "student@example.com",
                "Jane Doe",
                tId,
                "tenant-alpha",
                UUID.randomUUID(),
                List.of(role),
                Collections.emptySet()
        );
    }

    @Test
    @DisplayName("Rejects request when user principal is null")
    void rejectsNullPrincipal() {
        assertThatThrownBy(() -> controller.getStatus(null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("tenant context required");
    }

    @Test
    @DisplayName("Rejects request when tenant context is missing (null tenant ID)")
    void rejectsMissingTenantContext() {
        UserPrincipal principalNoTenant = createPrincipal(null, userId, "ROLE_STUDENT");

        assertThatThrownBy(() -> controller.getStatus(principalNoTenant))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("tenant context required");
    }

    @Test
    @DisplayName("Successfully returns onboarding status for authenticated student with tenant")
    void returnsStatusForValidPrincipal() {
        UserPrincipal principal = createPrincipal(tenantId, userId, "ROLE_STUDENT");
        OnboardingStatusResponse expected = new OnboardingStatusResponse(
                OnboardingStatus.IN_PROGRESS,
                true,
                true,
                false,
                false,
                List.of("TERMS_OF_SERVICE"),
                false,
                null,
                null,
                null
        );

        when(onboardingService.getOnboardingStatus(tenantId, userId)).thenReturn(expected);

        ResponseEntity<OnboardingStatusResponse> response = controller.getStatus(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().overallStatus()).isEqualTo(OnboardingStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Rejects avatar upload when malware scan detects threat")
    void rejectsAvatarOnMalwareDetection() {
        UserPrincipal principal = createPrincipal(tenantId, userId, "ROLE_STUDENT");
        byte[] bytes = new byte[]{1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", bytes);

        when(avatarValidationService.validateAvatarFile(file))
                .thenReturn(new AvatarValidationService.ValidatedAvatar("image/jpeg", "jpg", bytes, 200, 200));

        when(malwareScanningService.scanFileBytes(bytes))
                .thenReturn(new AvatarMalwareScanningService.ScanResult(ScanStatus.INFECTED, "Eicar-Test-Signature"));

        assertThatThrownBy(() -> controller.uploadAvatar(file, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Avatar file failed security scan");

        verify(storageService, never()).storeAvatar(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Allows avatar download when asset is clean and sets public caching headers")
    void streamsCleanAvatarWithCacheHeaders() throws Exception {
        UUID assetId = UUID.randomUUID();
        AvatarAsset asset = new AvatarAsset();
        asset.setId(assetId);
        asset.setContentType("image/png");
        asset.setScanStatus(ScanStatus.CLEAN);
        asset.setStorageObjectKey("avatars/t/u/file.png");

        when(avatarAssetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(storageService.loadAvatarResource("avatars/t/u/file.png"))
                .thenReturn(new org.springframework.core.io.ByteArrayResource(new byte[]{1, 2, 3}));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getAvatar(assetId, response);

        assertThat(response.getContentType()).isEqualTo("image/png");
        assertThat(response.getHeader("Cache-Control")).contains("public, max-age=86400");
    }

    @Test
    @DisplayName("Rejects avatar download if asset is quarantined or infected")
    void rejectsQuarantinedAvatarDownload() {
        UUID assetId = UUID.randomUUID();
        AvatarAsset asset = new AvatarAsset();
        asset.setId(assetId);
        asset.setContentType("image/png");
        asset.setScanStatus(ScanStatus.QUARANTINED);

        when(avatarAssetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThatThrownBy(() -> controller.getAvatar(assetId, response))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Avatar asset is not available");
    }
}
