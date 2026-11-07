package com.nexarats.academy.onboarding.web;

import com.nexarats.academy.common.security.UserPrincipal;
import com.nexarats.academy.onboarding.dto.*;
import com.nexarats.academy.onboarding.model.AvatarAsset;
import com.nexarats.academy.onboarding.model.ScanStatus;
import com.nexarats.academy.onboarding.repository.AvatarAssetRepository;
import com.nexarats.academy.onboarding.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/onboarding")
@PreAuthorize("hasAnyRole('STUDENT', 'EMPLOYEE', 'ACADEMY_ADMIN', 'SUPER_ADMIN')")
public class OnboardingController {

    private static final Logger log = LoggerFactory.getLogger(OnboardingController.class);

    private final StudentOnboardingService onboardingService;
    private final StudentProfileService profileService;
    private final LegalAgreementService legalAgreementService;
    private final AvatarValidationService avatarValidationService;
    private final AvatarMalwareScanningService malwareScanningService;
    private final AvatarStorageService storageService;
    private final AvatarAssetRepository avatarAssetRepository;

    public OnboardingController(
            StudentOnboardingService onboardingService,
            StudentProfileService profileService,
            LegalAgreementService legalAgreementService,
            AvatarValidationService avatarValidationService,
            AvatarMalwareScanningService malwareScanningService,
            AvatarStorageService storageService,
            AvatarAssetRepository avatarAssetRepository) {
        this.onboardingService = onboardingService;
        this.profileService = profileService;
        this.legalAgreementService = legalAgreementService;
        this.avatarValidationService = avatarValidationService;
        this.malwareScanningService = malwareScanningService;
        this.storageService = storageService;
        this.avatarAssetRepository = avatarAssetRepository;
    }

    @GetMapping("/status")
    public ResponseEntity<OnboardingStatusResponse> getStatus(@AuthenticationPrincipal UserPrincipal principal) {
        assertAuthenticated(principal);
        OnboardingStatusResponse response = onboardingService.getOnboardingStatus(
                principal.getTenantId(), principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<StudentProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        assertAuthenticated(principal);
        StudentProfileDto dto = profileService.getProfileDto(principal.getTenantId(), principal.getUserId());
        if (dto == null) {
            // Initialize on first access
            profileService.getOrCreateProfile(principal.getTenantId(), principal.getUserId());
            dto = profileService.getProfileDto(principal.getTenantId(), principal.getUserId());
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/profile")
    public ResponseEntity<StudentProfileDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        assertAuthenticated(principal);
        StudentProfileDto updated = profileService.updateProfile(
                principal.getTenantId(), principal.getUserId(), request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        assertAuthenticated(principal);

        // 1. MIME magic bytes and integrity validation
        AvatarValidationService.ValidatedAvatar validated = avatarValidationService.validateAvatarFile(file);

        // 2. Malware and heuristic scan
        AvatarMalwareScanningService.ScanResult scanResult = malwareScanningService.scanFileBytes(validated.fileBytes());
        if (scanResult.status() != ScanStatus.CLEAN) {
            log.warn("Avatar upload rejected for user {}: {}", principal.getUserId(), scanResult.details());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file failed security scan: " + scanResult.details());
        }

        // 3. Store file safely
        String storageKey = storageService.storeAvatar(
                principal.getTenantId(), principal.getUserId(), validated.fileExtension(), validated.fileBytes());

        // 4. Record AvatarAsset entity
        AvatarAsset asset = new AvatarAsset(
                principal.getTenantId(),
                principal.getUserId(),
                storageKey,
                file.getOriginalFilename(),
                validated.detectedContentType(),
                validated.fileBytes().length
        );
        asset.setScanStatus(scanResult.status());
        asset.setScanDetails(scanResult.details());
        asset.setScannedAt(Instant.now());
        asset = avatarAssetRepository.save(asset);

        // 5. Link to StudentProfile
        profileService.linkAvatarAsset(principal.getTenantId(), principal.getUserId(), asset.getId());

        String avatarUrl = profileService.resolveAvatarUrl(asset.getId());

        AvatarUploadResponse response = new AvatarUploadResponse(
                asset.getId(),
                storageKey,
                asset.getContentType(),
                asset.getFileSizeBytes(),
                asset.getScanStatus(),
                asset.getScanDetails(),
                avatarUrl
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/avatar/{assetId}")
    @PreAuthorize("permitAll()")
    public void getAvatar(@PathVariable UUID assetId, HttpServletResponse response) {
        AvatarAsset asset = avatarAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar asset not found: " + assetId));

        if (asset.getScanStatus() != ScanStatus.CLEAN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Avatar asset is not available.");
        }

        Resource resource = storageService.loadAvatarResource(asset.getStorageObjectKey());
        if (resource == null || !resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar file could not be found.");
        }

        response.setContentType(asset.getContentType());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=86400");
        try (InputStream in = resource.getInputStream()) {
            StreamUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("Failed to stream avatar asset {}", assetId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to stream avatar file.");
        }
    }

    @GetMapping("/agreements")
    public ResponseEntity<List<LegalAgreementDto>> getAgreements(@AuthenticationPrincipal UserPrincipal principal) {
        assertAuthenticated(principal);
        List<LegalAgreementDto> agreements = legalAgreementService.getAgreementsForStudent(
                principal.getTenantId(), principal.getUserId());
        return ResponseEntity.ok(agreements);
    }

    @PostMapping("/agreements/{agreementId}/accept")
    public ResponseEntity<LegalAgreementDto> acceptAgreement(
            @PathVariable UUID agreementId,
            @Valid @RequestBody AcceptAgreementRequest request,
            HttpServletRequest httpRequest,
            @AuthenticationPrincipal UserPrincipal principal) {
        assertAuthenticated(principal);

        String clientIp = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LegalAgreementDto result = legalAgreementService.acceptAgreement(
                principal.getTenantId(),
                principal.getUserId(),
                agreementId,
                request.agreementVersion(),
                clientIp,
                userAgent
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/complete")
    public ResponseEntity<CompleteOnboardingResponse> completeOnboarding(@AuthenticationPrincipal UserPrincipal principal) {
        assertAuthenticated(principal);
        CompleteOnboardingResponse response = onboardingService.completeOnboarding(
                principal.getTenantId(), principal.getUserId());
        return ResponseEntity.ok(response);
    }

    private void assertAuthenticated(UserPrincipal principal) {
        if (principal == null || principal.getUserId() == null || principal.getTenantId() == null) {
            throw new AccessDeniedException("Authentication and tenant context required to access onboarding resources.");
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
