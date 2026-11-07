package com.nexarats.academy.onboarding;

import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.repository.UserRepository;
import com.nexarats.academy.onboarding.dto.StudentProfileDto;
import com.nexarats.academy.onboarding.dto.UpdateProfileRequest;
import com.nexarats.academy.onboarding.model.AvatarAsset;
import com.nexarats.academy.onboarding.model.OnboardingStatus;
import com.nexarats.academy.onboarding.model.ScanStatus;
import com.nexarats.academy.onboarding.model.StudentProfile;
import com.nexarats.academy.onboarding.repository.AvatarAssetRepository;
import com.nexarats.academy.onboarding.repository.StudentProfileRepository;
import com.nexarats.academy.onboarding.service.AvatarStorageService;
import com.nexarats.academy.onboarding.service.StudentProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    @Mock
    private StudentProfileRepository profileRepository;

    @Mock
    private AvatarAssetRepository avatarAssetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AvatarStorageService storageService;

    private StudentProfileService profileService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        profileService = new StudentProfileService(
                profileRepository,
                avatarAssetRepository,
                userRepository,
                storageService
        );
    }

    @Test
    @DisplayName("Successfully links owned avatar asset to student profile")
    void linksOwnedAvatarAssetSuccessfully() {
        UUID assetId = UUID.randomUUID();
        AvatarAsset asset = new AvatarAsset(tenantId, userId, "avatars/t/u/1.jpg", "1.jpg", "image/jpeg", 1024);
        asset.setId(assetId);
        asset.setScanStatus(ScanStatus.CLEAN);

        StudentProfile profile = new StudentProfile(tenantId, userId);

        when(avatarAssetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(profileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(StudentProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentProfile updated = profileService.linkAvatarAsset(tenantId, userId, assetId);

        assertThat(updated.getAvatarAssetId()).isEqualTo(assetId);
        assertThat(updated.getOnboardingStatus()).isEqualTo(OnboardingStatus.IN_PROGRESS);
        verify(profileRepository).save(profile);
    }

    @Test
    @DisplayName("Security: Rejects linking avatar asset owned by a different user with 403 Forbidden")
    void rejectsLinkingAvatarAssetOwnedByDifferentUser() {
        UUID assetId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        AvatarAsset foreignAsset = new AvatarAsset(tenantId, otherUserId, "avatars/t/other/1.jpg", "1.jpg", "image/jpeg", 1024);
        foreignAsset.setId(assetId);

        when(avatarAssetRepository.findById(assetId)).thenReturn(Optional.of(foreignAsset));

        assertThatThrownBy(() -> profileService.linkAvatarAsset(tenantId, userId, assetId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot link an avatar asset owned by another user");
    }

    @Test
    @DisplayName("Security: Rejects linking avatar asset from a different tenant with 403 Forbidden")
    void rejectsLinkingAvatarAssetFromDifferentTenant() {
        UUID assetId = UUID.randomUUID();
        UUID otherTenantId = UUID.randomUUID();
        AvatarAsset foreignTenantAsset = new AvatarAsset(otherTenantId, userId, "avatars/other/u/1.jpg", "1.jpg", "image/jpeg", 1024);
        foreignTenantAsset.setId(assetId);

        when(avatarAssetRepository.findById(assetId)).thenReturn(Optional.of(foreignTenantAsset));

        assertThatThrownBy(() -> profileService.linkAvatarAsset(tenantId, userId, assetId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot link an avatar asset from another tenant");
    }

    @Test
    @DisplayName("Safe Replacement: Cleans up previous avatar asset when a new avatar is linked")
    void cleansUpOldAvatarOnReplacement() {
        UUID oldAssetId = UUID.randomUUID();
        UUID newAssetId = UUID.randomUUID();

        AvatarAsset oldAsset = new AvatarAsset(tenantId, userId, "avatars/t/u/old.jpg", "old.jpg", "image/jpeg", 1024);
        oldAsset.setId(oldAssetId);

        AvatarAsset newAsset = new AvatarAsset(tenantId, userId, "avatars/t/u/new.jpg", "new.jpg", "image/jpeg", 2048);
        newAsset.setId(newAssetId);

        StudentProfile profile = new StudentProfile(tenantId, userId);
        profile.setAvatarAssetId(oldAssetId);

        when(avatarAssetRepository.findById(newAssetId)).thenReturn(Optional.of(newAsset));
        when(avatarAssetRepository.findById(oldAssetId)).thenReturn(Optional.of(oldAsset));
        when(profileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(StudentProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentProfile updated = profileService.linkAvatarAsset(tenantId, userId, newAssetId);

        assertThat(updated.getAvatarAssetId()).isEqualTo(newAssetId);
        assertThat(oldAsset.isActive()).isFalse();
        verify(avatarAssetRepository).save(oldAsset);
        verify(storageService).deleteAvatar("avatars/t/u/old.jpg");
    }
}

