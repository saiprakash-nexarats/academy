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
        List<LegalAgreement> agreements = agreementRepository.findActiveAgreementsForTenant(tenantId);
        List<StudentAgreementAcceptance> acceptances = (tenantId != null)
                ? acceptanceRepository.findByTenantIdAndStudentUserId(tenantId, studentUserId)
                : acceptanceRepository.findByStudentUserId(studentUserId);

        Map<UUID, StudentAgreementAcceptance> acceptanceMap = acceptances.stream()
                .collect(Collectors.toMap(
                        StudentAgreementAcceptance::getAgreementId,
                        a -> a,
                        (existing, replacement) -> existing.getAcceptedAt().isAfter(replacement.getAcceptedAt()) ? existing : replacement
                ));

        return agreements.stream()
                .map(ag -> {
                    StudentAgreementAcceptance acc = acceptanceMap.get(ag.getId());
                    boolean accepted = acc != null && ag.getVersion().equalsIgnoreCase(acc.getAgreementVersion());
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

        if (!agreement.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agreement is no longer active.");
        }

        if (!agreement.getVersion().equalsIgnoreCase(agreementVersion.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Submitted agreement version (" + agreementVersion + ") does not match current version (" + agreement.getVersion() + ").");
        }

        // Verify SHA-256 integrity
        verifyAgreementIntegrity(agreement);

        // Check if already accepted
        boolean alreadyAccepted = (tenantId != null)
                ? acceptanceRepository.existsByTenantIdAndStudentUserIdAndAgreementIdAndAgreementVersion(
                tenantId, studentUserId, agreementId, agreement.getVersion())
                : acceptanceRepository.existsByStudentUserIdAndAgreementIdAndAgreementVersion(
                studentUserId, agreementId, agreement.getVersion());

        StudentAgreementAcceptance acceptance;
        if (alreadyAccepted) {
            log.info("Student {} already accepted agreement {} version {}", studentUserId, agreementId, agreement.getVersion());
            return LegalAgreementDto.fromEntity(agreement, true, Instant.now());
        } else {
            acceptance = new StudentAgreementAcceptance(
                    tenantId,
                    studentUserId,
                    agreementId,
                    agreement.getVersion(),
                    ipAddress,
                    userAgent
            );
            acceptance = acceptanceRepository.save(acceptance);
            log.info("Recorded agreement acceptance: user={}, agreement={}, version={}, ip={}",
                    studentUserId, agreementId, agreement.getVersion(), ipAddress);
        }

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
                log.error("Agreement content hash mismatch! Expected: {}, Calculated: {}", agreement.getContentHash(), calculatedHash);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Agreement content integrity check failed.");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest algorithm unavailable", e);
        }
    }
}
