package com.nexarats.academy.onboarding.service;

import com.nexarats.academy.onboarding.dto.LegalAgreementDto;
import com.nexarats.academy.onboarding.model.LegalAgreement;
import com.nexarats.academy.onboarding.model.LegalAgreementType;
import com.nexarats.academy.onboarding.model.StudentAgreementAcceptance;
import com.nexarats.academy.onboarding.repository.LegalAgreementRepository;
import com.nexarats.academy.onboarding.repository.StudentAgreementAcceptanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LegalAgreementService {

    private static final Logger log = LoggerFactory.getLogger(LegalAgreementService.class);

    private final LegalAgreementRepository agreementRepository;
    private final StudentAgreementAcceptanceRepository acceptanceRepository;

    public LegalAgreementService(LegalAgreementRepository agreementRepository,
                                 StudentAgreementAcceptanceRepository acceptanceRepository) {
        this.agreementRepository = agreementRepository;
        this.acceptanceRepository = acceptanceRepository;
    }

    @Transactional(readOnly = true)
    public List<LegalAgreementDto> getAgreementsForStudent(UUID tenantId, UUID studentUserId) {
        Instant now = Instant.now();
        List<LegalAgreement> allAgreements = agreementRepository.findActiveAgreementsForTenant(tenantId, now);

        // Group by agreement type and resolve the most specific applicable agreement:
        // Prefer tenant-specific agreement over global agreement; pick newest effective
        Map<LegalAgreementType, LegalAgreement> resolvedAgreements = new EnumMap<>(LegalAgreementType.class);
        for (LegalAgreement ag : allAgreements) {
            LegalAgreementType type = ag.getAgreementType();
            if (!resolvedAgreements.containsKey(type)) {
                resolvedAgreements.put(type, ag);
            } else {
                LegalAgreement current = resolvedAgreements.get(type);
                boolean currentIsTenantSpecific = tenantId != null && tenantId.equals(current.getTenantId());
                boolean newIsTenantSpecific = tenantId != null && tenantId.equals(ag.getTenantId());

                if (!currentIsTenantSpecific && newIsTenantSpecific) {
                    resolvedAgreements.put(type, ag);
                }
            }
        }

        List<LegalAgreement> applicableAgreements = new ArrayList<>(resolvedAgreements.values());
        applicableAgreements.sort(Comparator.comparing(LegalAgreement::getAgreementType));

        List<StudentAgreementAcceptance> acceptances = (tenantId != null)
                ? acceptanceRepository.findByTenantIdAndStudentUserId(tenantId, studentUserId)
                : acceptanceRepository.findByStudentUserId(studentUserId);

        Map<UUID, StudentAgreementAcceptance> acceptanceMap = acceptances.stream()
                .collect(Collectors.toMap(
                        StudentAgreementAcceptance::getAgreementId,
                        a -> a,
                        (existing, replacement) -> existing.getAcceptedAt().isBefore(replacement.getAcceptedAt()) ? existing : replacement
                ));

        return applicableAgreements.stream()
                .map(ag -> {
                    StudentAgreementAcceptance acc = acceptanceMap.get(ag.getId());
                    boolean accepted = acc != null
                            && !acc.isLegacyUnverified()
                            && ag.getVersion().equalsIgnoreCase(acc.getAgreementVersion())
                            && ag.getContentHash().equalsIgnoreCase(acc.getContentHash());
                    Instant acceptedAt = (acc != null && accepted) ? acc.getAcceptedAt() : null;
                    return LegalAgreementDto.fromEntity(ag, accepted, acceptedAt);
                })
                .toList();
    }

    @Transactional
    public LegalAgreementDto acceptAgreement(UUID tenantId, UUID studentUserId, UUID agreementId,
                                            String agreementVersion, String ipAddress, String userAgent) {
        LegalAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Legal agreement not found: " + agreementId));

        // 1. Strict Tenant Isolation Check
        if (agreement.getTenantId() != null && !agreement.getTenantId().equals(tenantId)) {
            log.warn("Cross-tenant agreement acceptance rejected: student tenant={}, agreement tenant={}",
                    tenantId, agreement.getTenantId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Agreement does not belong to the authenticated tenant.");
        }

        Instant now = Instant.now();

        // 2. Active & Effective Date Checks
        if (!agreement.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agreement is no longer active.");
        }
        if (agreement.getEffectiveDate() != null && agreement.getEffectiveDate().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agreement is not yet effective.");
        }

        // 3. Version Check
        if (!agreement.getVersion().equalsIgnoreCase(agreementVersion.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Submitted agreement version (" + agreementVersion + ") does not match current version (" + agreement.getVersion() + ").");
        }

        // 4. Verify SHA-256 Cryptographic Integrity
        verifyAgreementIntegrity(agreement);

        // 5. Idempotent check preserving original acceptance timestamp
        Optional<StudentAgreementAcceptance> existingOpt = acceptanceRepository
                .findByTenantIdAndStudentUserIdAndAgreementIdAndAgreementVersion(
                        tenantId, studentUserId, agreementId, agreement.getVersion());

        if (existingOpt.isPresent()) {
            StudentAgreementAcceptance existing = existingOpt.get();
            log.info("Student {} already accepted agreement {} version {} at {}",
                    studentUserId, agreementId, agreement.getVersion(), existing.getAcceptedAt());
            return LegalAgreementDto.fromEntity(agreement, true, existing.getAcceptedAt());
        }

        // 6. Record Immutable Acceptance Audit Entry with Content Hash
        StudentAgreementAcceptance acceptance = new StudentAgreementAcceptance(
                tenantId,
                studentUserId,
                agreementId,
                agreement.getVersion(),
                agreement.getContentHash(),
                ipAddress,
                userAgent
        );
        acceptance = acceptanceRepository.save(acceptance);
        log.info("Recorded agreement acceptance: user={}, agreement={}, version={}, hash={}, ip={}",
                studentUserId, agreementId, agreement.getVersion(), agreement.getContentHash(), ipAddress);

        return LegalAgreementDto.fromEntity(agreement, true, acceptance.getAcceptedAt());
    }

    @Transactional(readOnly = true)
    public boolean areAllMandatoryAgreementsAccepted(UUID tenantId, UUID studentUserId, List<String> missingAgreementsOut) {
        List<LegalAgreementDto> dtos = getAgreementsForStudent(tenantId, studentUserId);

        Set<LegalAgreementType> requiredTypes = Set.of(
                LegalAgreementType.TERMS_OF_SERVICE,
                LegalAgreementType.ACADEMIC_INTEGRITY,
                LegalAgreementType.CODE_OF_CONDUCT
        );

        Map<LegalAgreementType, Boolean> acceptedStatus = new EnumMap<>(LegalAgreementType.class);
        for (LegalAgreementDto dto : dtos) {
            if (dto.isAccepted()) {
                acceptedStatus.put(dto.agreementType(), true);
            }
        }

        boolean allAccepted = true;
        for (LegalAgreementType req : requiredTypes) {
            if (!Boolean.TRUE.equals(acceptedStatus.get(req))) {
                allAccepted = false;
                if (missingAgreementsOut != null) {
                    missingAgreementsOut.add(req.name());
                }
            }
        }

        return allAccepted;
    }

    private void verifyAgreementIntegrity(LegalAgreement agreement) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(agreement.getContent().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            String calculatedHash = sb.toString();

            if (!calculatedHash.equalsIgnoreCase(agreement.getContentHash())) {
                log.error("Agreement content hash mismatch! Expected: {}, Calculated: {}",
                        agreement.getContentHash(), calculatedHash);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Cryptographic integrity check failed for legal agreement.");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SHA-256 digest unavailable.");
        }
    }
}
