package com.nexarats.academy.onboarding;

import com.nexarats.academy.common.security.UserPrincipal;
import com.nexarats.academy.onboarding.dto.*;
import com.nexarats.academy.onboarding.model.*;
import com.nexarats.academy.onboarding.repository.AvatarAssetRepository;
import com.nexarats.academy.onboarding.service.*;
import com.nexarats.academy.onboarding.web.OnboardingController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    @DisplayName("New AvatarAsset constructor defaults strictly to PENDING, never CLEAN")
    void avatarAssetDefaultsToPending() {
        AvatarAsset asset = new AvatarAsset(tenantId, userId, "avatars/key.jpg", "pic.jpg", "image/jpeg", 1024);
        assertThat(asset.getScanStatus()).isEqualTo(ScanStatus.PENDING);
    }

    @Test
    @DisplayName("Rejects avatar upload when malware scan detects threat and deletes file")
    void rejectsAvatarOnMalwareDetection() {
        UserPrincipal principal = createPrincipal(tenantId, userId, "ROLE_STUDENT");
        byte[] bytes = new byte[]{1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", bytes);

        when(avatarValidationService.validateAvatarFile(file))
                .thenReturn(new AvatarValidationService.ValidatedAvatar("image/jpeg", "jpg", bytes, 200, 200));

        when(storageService.storeAvatar(tenantId, userId, "jpg", bytes))
                .thenReturn("avatars/quarantine-key.jpg");

        AvatarAsset pendingAsset = new AvatarAsset(tenantId, userId, "avatars/quarantine-key.jpg", "test.jpg", "image/jpeg", bytes.length);
        pendingAsset.setId(UUID.randomUUID());
        when(avatarAssetRepository.save(any(AvatarAsset.class))).thenReturn(pendingAsset);

        when(malwareScanningService.scanFileBytes(bytes))
                .thenReturn(new AvatarMalwareScanningService.ScanResult(ScanStatus.INFECTED, "Eicar-Test-Signature"));

        assertThatThrownBy(() -> controller.uploadAvatar(file, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Malware detected");

        verify(storageService).deleteAvatar("avatars/quarantine-key.jpg");
        verify(profileService, never()).linkAvatarAsset(any(), any(), any());
    }

    @Test
    @DisplayName("Avatar removal endpoint unlinks avatar and returns 204 No Content")
    void removeAvatarEndpointUnlinksAvatar() {
        UserPrincipal principal = createPrincipal(tenantId, userId, "ROLE_STUDENT");

        ResponseEntity<Void> response = controller.removeAvatar(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(profileService).unlinkAvatar(tenantId, userId);
    }

    @Test
    @DisplayName("Presigned URL endpoint returns short-lived, ownership-bound upload specification")
    void generatesPresignedUploadUrl() {
        UserPrincipal principal = createPrincipal(tenantId, userId, "ROLE_STUDENT");
        AvatarStorageService.PreSignedUploadSpec spec = new AvatarStorageService.PreSignedUploadSpec(
                "/api/v1/onboarding/avatar/direct-upload?key=avatars/spec.jpg",
                "avatars/spec.jpg",
                "PUT",
                Instant.now().plusSeconds(900),
                "sig-123"
        );

        when(storageService.generatePreSignedUploadSpec(tenantId, userId, "jpg", "image/jpeg"))
                .thenReturn(spec);

        ResponseEntity<AvatarStorageService.PreSignedUploadSpec> resp = controller.generatePresignedUploadUrl("jpg", "image/jpeg", principal);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().uploadUrl()).contains("direct-upload");
        assertThat(resp.getBody().httpMethod()).isEqualTo("PUT");
    }

    @Test
    @DisplayName("Allows avatar download when asset is clean and sets public caching headers")
    void streamsCleanAvatarWithCacheHeaders() throws Exception {
        UUID assetId = UUID.randomUUID();
        AvatarAsset asset = new AvatarAsset();
        asset.setId(assetId);
        asset.setContentType("image/png");
        asset.setScanStatus(ScanStatus.CLEAN);
        asset.setActive(true);
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
        asset.setActive(true);

        when(avatarAssetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThatThrownBy(() -> controller.getAvatar(assetId, response))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Avatar asset is not available");
    }

    @Test
    @DisplayName("Security: Client IP extraction does NOT trust spoofed X-Forwarded-For when proxy trust disabled")
    void doesNotTrustSpoofedIpWhenProxyTrustDisabled() {
        ReflectionTestUtils.setField(controller, "trustProxyHeaders", false);
        UserPrincipal principal = createPrincipal(tenantId, userId, "ROLE_STUDENT");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        request.addHeader("X-Forwarded-For", "1.1.1.1, 2.2.2.2");

        UUID agId = UUID.randomUUID();
        when(legalAgreementService.acceptAgreement(eq(tenantId), eq(userId), eq(agId), eq("1.0"), eq("192.168.1.100"), any()))
                .thenReturn(new LegalAgreementDto(agId, LegalAgreementType.TERMS_OF_SERVICE, "1.0", "TOS", "content", "hash", Instant.now(), true, Instant.now()));

        controller.acceptAgreement(agId, new AcceptAgreementRequest("1.0"), request, principal);

        // Verification: The service received "192.168.1.100" (remote TCP addr), NOT the spoofed "1.1.1.1"!
        verify(legalAgreementService).acceptAgreement(eq(tenantId), eq(userId), eq(agId), eq("1.0"), eq("192.168.1.100"), any());
    }
}
