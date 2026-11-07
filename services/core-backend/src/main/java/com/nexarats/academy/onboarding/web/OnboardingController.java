package com.nexarats.academy.onboarding.web;

import com.nexarats.academy.common.security.UserPrincipal;
import com.nexarats.academy.onboarding.dto.*;
import com.nexarats.academy.onboarding.model.*;
import com.nexarats.academy.onboarding.repository.AvatarAssetRepository;
import com.nexarats.academy.onboarding.repository.AvatarUploadTicketRepository;
import com.nexarats.academy.onboarding.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/onboarding")
@PreAuthorize("hasAnyRole('STUDENT', 'EMPLOYEE', 'ACADEMY_ADMIN', 'SUPER_ADMIN')")
public class OnboardingController {

    private static final Logger log = LoggerFactory.getLogger(OnboardingController.class);

    private static final Pattern IPV4_OR_IPV6 = Pattern.compile(
            "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"
    );

    @Value("${nexarats.security.trust-proxy-headers:false}")
    private boolean trustProxyHeaders;

    private final StudentOnboardingService onboardingService;
    private final StudentProfileService profileService;
    private final LegalAgreementService legalAgreementService;
    private final AvatarValidationService avatarValidationService;
    private final AvatarMalwareScanningService malwareScanningService;
    private final AvatarStorageService storageService;
    private final AvatarAssetRepository avatarAssetRepository;
    private final AvatarUploadTicketRepository uploadTicketRepository;

    public OnboardingController(
            StudentOnboardingService onboardingService,
            StudentProfileService profileService,
            LegalAgreementService legalAgreementService,
            AvatarValidationService avatarValidationService,
            AvatarMalwareScanningService malwareScanningService,
            AvatarStorageService storageService,
            AvatarAssetRepository avatarAssetRepository) {
        this(onboardingService, profileService, legalAgreementService, avatarValidationService,
                malwareScanningService, storageService, avatarAssetRepository, null);
    }

    public OnboardingController(
            StudentOnboardingService onboardingService,
            StudentProfileService profileService,
            LegalAgreementService legalAgreementService,
            AvatarValidationService avatarValidationService,
            AvatarMalwareScanningService malwareScanningService,
            AvatarStorageService storageService,
            AvatarAssetRepository avatarAssetRepository,
            AvatarUploadTicketRepository uploadTicketRepository) {
        this.onboardingService = onboardingService;
        this.profileService = profileService;
        this.legalAgreementService = legalAgreementService;
        this.avatarValidationService = avatarValidationService;
        this.malwareScanningService = malwareScanningService;
        this.storageService = storageService;
        this.avatarAssetRepository = avatarAssetRepository;
        this.uploadTicketRepository = uploadTicketRepository;
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
        StudentProfileDto profileDto = profileService.getProfileDto(
                principal.getTenantId(), principal.getUserId());
        if (profileDto == null) {
            StudentProfile initial = profileService.getOrCreateProfile(
                    principal.getTenantId(), principal.getUserId());
            profileDto = StudentProfileDto.fromEntity(initial, null);
        }
        return ResponseEntity.ok(profileDto);
    }

    @PutMapping("/profile")
    public ResponseEntity<StudentProfileDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        assertAuthenticated(principal);
        StudentProfileDto updated = profileService.updateProfile(
                principal.getTenantId(), principal.getUserId(), request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/avatar/presigned-url")
    public ResponseEntity<AvatarStorageService.PreSignedUploadSpec> generatePresignedUploadUrl(
            @RequestParam(defaultValue = "jpg") String extension,
            @RequestParam(defaultValue = "image/jpeg") String contentType,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        assertAuthenticated(principal);

        // Pre-validate format and content-type before generating spec or issuing ticket
        storageService.validateExtensionAndContentType(extension, contentType);

        AvatarStorageService.PreSignedUploadSpec spec = storageService.generatePreSignedUploadSpec(
                principal.getTenantId(), principal.getUserId(), extension, contentType);

        String cleanExt = extension.toLowerCase().replace(".", "").trim();
        AvatarUploadTicket ticket = new AvatarUploadTicket(
                principal.getTenantId(),
                principal.getUserId(),
                spec.storageObjectKey(),
                "avatar." + cleanExt,
                contentType,
                cleanExt,
                spec.expiresAt()
        );
        uploadTicketRepository.save(ticket);
        log.info("Issued and recorded avatar upload ticket {} for user {} quarantineKey {}",
                ticket.getId(), principal.getUserId(), spec.storageObjectKey());

        return ResponseEntity.ok(spec);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        assertAuthenticated(principal);

        // 1. Strict validation of image magic-bytes, format, size and dimensions
        AvatarValidationService.ValidatedAvatar validated = avatarValidationService.validateAvatarFile(file);

        // 2. Persist avatar asset initially as PENDING (never automatically CLEAN)
        String storageKey = storageService.storeAvatar(
                principal.getTenantId(), principal.getUserId(), validated.fileExtension(), validated.fileBytes());

        AvatarAsset asset = new AvatarAsset(
                principal.getTenantId(),
                principal.getUserId(),
                storageKey,
                file.getOriginalFilename(),
                validated.detectedContentType(),
                validated.fileBytes().length
        );
        asset.setScanStatus(ScanStatus.PENDING);
        asset = avatarAssetRepository.save(asset);

        // 3. Antivirus threat verification via ClamAV daemon
        AvatarMalwareScanningService.ScanResult scanResult = malwareScanningService.scanFileBytes(validated.fileBytes());

        if (scanResult.status() == ScanStatus.CLEAN) {
            asset.approveAsClean(scanResult.details());
            asset = avatarAssetRepository.save(asset);

            // Link clean avatar asset to profile
            profileService.linkAvatarAsset(principal.getTenantId(), principal.getUserId(), asset.getId());
        } else if (scanResult.status() == ScanStatus.QUARANTINED) {
            asset.quarantine(scanResult.details());
            avatarAssetRepository.save(asset);
            log.warn("Avatar upload quarantined for user {}: {}", principal.getUserId(), scanResult.details());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Avatar file quarantined: " + scanResult.details());
        } else {
            // INFECTED
            asset.markInfected(scanResult.details());
            avatarAssetRepository.save(asset);
            storageService.deleteAvatar(storageKey);
            log.warn("Avatar upload rejected for user {}: {}", principal.getUserId(), scanResult.details());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Malware detected in avatar payload: " + scanResult.details());
        }

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

    @PostMapping("/avatar/confirm-upload")
    public ResponseEntity<AvatarUploadResponse> confirmPresignedUpload(
            @Valid @RequestBody ConfirmAvatarUploadRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        assertAuthenticated(principal);

        String quarantineKey = request.storageObjectKey();

        // 1. Look up issued upload ticket to enforce ticket-based authorization & quarantine isolation
        AvatarUploadTicket ticket = uploadTicketRepository.findByQuarantineKey(quarantineKey)
                .orElseThrow(() -> {
                    log.warn("Confirm upload rejected: No ticket issued for key {}", quarantineKey);
                    return new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid or unauthorized upload ticket.");
                });

        // 2. Strict ownership binding: caller must match ticket user and tenant
        if (!ticket.getUserId().equals(principal.getUserId()) ||
                (ticket.getTenantId() != null && !ticket.getTenantId().equals(principal.getTenantId()))) {
            log.warn("Confirm upload ownership spoofing rejected: user={}, ticketUser={}, ticketTenant={}",
                    principal.getUserId(), ticket.getUserId(), ticket.getTenantId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized upload ticket access.");
        }

        // 3. Single-use and expiration enforcement
        if (!ticket.isIssued()) {
            log.warn("Confirm upload rejected: Ticket {} status is {} (re-use defense)", ticket.getId(), ticket.getStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload ticket has already been used or expired.");
        }

        if (ticket.isExpired()) {
            ticket.expire();
            uploadTicketRepository.save(ticket);
            storageService.deleteAvatar(quarantineKey);
            log.warn("Confirm upload rejected: Ticket {} has expired", ticket.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload ticket has expired.");
        }

        // 4. Fetch bounded bytes from quarantined storage (HEAD check + max 5MB streaming limit)
        byte[] bytes;
        try {
            bytes = storageService.getAvatarBytes(quarantineKey, AvatarStorageService.MAX_AVATAR_SIZE_BYTES);
        } catch (ResponseStatusException e) {
            ticket.expire();
            uploadTicketRepository.save(ticket);
            throw e;
        }

        // 5. Validate MIME magic bytes, size and dimensions
        String filename = request.originalFilename() != null && !request.originalFilename().isBlank()
                ? request.originalFilename()
                : ticket.getOriginalFilename();
        AvatarValidationService.ValidatedAvatar validated;
        try {
            validated = avatarValidationService.validateAvatarBytes(bytes, filename);
        } catch (ResponseStatusException e) {
            ticket.expire();
            uploadTicketRepository.save(ticket);
            storageService.deleteAvatar(quarantineKey);
            throw e;
        }

        // 6. ClamAV malware threat scan
        AvatarMalwareScanningService.ScanResult scanResult = malwareScanningService.scanFileBytes(bytes);

        if (scanResult.status() == ScanStatus.CLEAN) {
            // Promote quarantined avatar bytes to a brand-new, unguessable approved key.
            // This guarantees the client's pre-signed PUT URL cannot overwrite approved content!
            String approvedKey = storageService.promoteQuarantinedAvatar(
                    quarantineKey,
                    principal.getTenantId(),
                    principal.getUserId(),
                    ticket.getFileExtension(),
                    bytes
            );

            ticket.confirm(approvedKey);
            uploadTicketRepository.save(ticket);

            AvatarAsset asset = new AvatarAsset(
                    principal.getTenantId(),
                    principal.getUserId(),
                    approvedKey,
                    filename,
                    validated.detectedContentType(),
                    bytes.length
            );
            asset.approveAsClean(scanResult.details());
            asset = avatarAssetRepository.save(asset);

            profileService.linkAvatarAsset(principal.getTenantId(), principal.getUserId(), asset.getId());

            String avatarUrl = profileService.resolveAvatarUrl(asset.getId());
            AvatarUploadResponse response = new AvatarUploadResponse(
                    asset.getId(),
                    approvedKey,
                    asset.getContentType(),
                    asset.getFileSizeBytes(),
                    asset.getScanStatus(),
                    asset.getScanDetails(),
                    avatarUrl
            );

            log.info("Successfully confirmed and promoted avatar {} for user {}", asset.getId(), principal.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (scanResult.status() == ScanStatus.QUARANTINED) {
            ticket.markQuarantined();
            uploadTicketRepository.save(ticket);
            storageService.deleteAvatar(quarantineKey);
            log.warn("Presigned avatar upload quarantined for user {}: {}", principal.getUserId(), scanResult.details());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file quarantined: " + scanResult.details());
        } else {
            // INFECTED
            ticket.markInfected();
            uploadTicketRepository.save(ticket);
            storageService.deleteAvatar(quarantineKey);
            log.warn("Presigned avatar upload rejected for user {}: {}", principal.getUserId(), scanResult.details());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malware detected in avatar payload: " + scanResult.details());
        }
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<Void> removeAvatar(@AuthenticationPrincipal UserPrincipal principal) {
        assertAuthenticated(principal);
        profileService.unlinkAvatar(principal.getTenantId(), principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/avatar/{assetId}")
    @PreAuthorize("permitAll()")
    public void getAvatar(@PathVariable UUID assetId, HttpServletResponse response) {
        AvatarAsset asset = avatarAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar asset not found: " + assetId));

        if (!asset.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar asset has been deleted or deactivated.");
        }

        if (asset.getScanStatus() != ScanStatus.CLEAN) {
            log.warn("Attempted access to unapproved avatar asset {} with scanStatus {}", assetId, asset.getScanStatus());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Avatar asset is not available.");
        }

        Resource resource = storageService.loadAvatarResource(asset.getStorageObjectKey());
        if (resource == null || !resource.exists()) {
            log.error("Avatar asset {} points to missing storage key: {}", assetId, asset.getStorageObjectKey());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar resource file not found on storage.");
        }

        try {
            response.setContentType(asset.getContentType());
            response.setContentLengthLong(asset.getFileSizeBytes());
            response.setHeader("Cache-Control", "public, max-age=86400, must-revalidate");
            response.setHeader("ETag", "\"" + asset.getId() + "\"");

            try (InputStream in = resource.getInputStream()) {
                StreamUtils.copy(in, response.getOutputStream());
                response.flushBuffer();
            }
        } catch (IOException e) {
            log.error("Failed to stream avatar asset {}: {}", assetId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading avatar file.");
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
            HttpServletRequest servletRequest,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        assertAuthenticated(principal);

        String ipAddress = resolveClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        LegalAgreementDto accepted = legalAgreementService.acceptAgreement(
                principal.getTenantId(),
                principal.getUserId(),
                agreementId,
                request.agreementVersion(),
                ipAddress,
                userAgent
        );
        return ResponseEntity.ok(accepted);
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

    private String resolveClientIp(HttpServletRequest request) {
        if (trustProxyHeaders) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                String firstIp = forwarded.split(",")[0].trim();
                if (IPV4_OR_IPV6.matcher(firstIp).matches()) {
                    return firstIp;
                }
            }
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isBlank() && IPV4_OR_IPV6.matcher(realIp.trim()).matches()) {
                return realIp.trim();
            }
        }
        return request.getRemoteAddr();
    }
}
