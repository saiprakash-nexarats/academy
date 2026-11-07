package com.nexarats.academy.onboarding;

import com.nexarats.academy.onboarding.dto.LegalAgreementDto;
import com.nexarats.academy.onboarding.model.LegalAgreement;
import com.nexarats.academy.onboarding.model.LegalAgreementType;
import com.nexarats.academy.onboarding.model.StudentAgreementAcceptance;
import com.nexarats.academy.onboarding.repository.LegalAgreementRepository;
import com.nexarats.academy.onboarding.repository.StudentAgreementAcceptanceRepository;
import com.nexarats.academy.onboarding.service.LegalAgreementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegalAgreementServiceTest {

    @Mock
    private LegalAgreementRepository agreementRepository;

    @Mock
    private StudentAgreementAcceptanceRepository acceptanceRepository;

    private LegalAgreementService agreementService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        agreementService = new LegalAgreementService(agreementRepository, acceptanceRepository);
    }

    private LegalAgreement createAgreement(LegalAgreementType type, String version, String content, String hash) {
        LegalAgreement ag = new LegalAgreement();
        ag.setId(UUID.randomUUID());
        ag.setAgreementType(type);
        ag.setVersion(version);
        ag.setTitle("Test " + type.name());
        ag.setContent(content);
        ag.setContentHash(hash);
        ag.setActive(true);
        ag.setEffectiveDate(Instant.now());
        return ag;
    }

    @Test
    @DisplayName("Lists agreements with accurate acceptance flags for student")
    void listsAgreementsWithAcceptanceFlags() {
        LegalAgreement tos = createAgreement(
                LegalAgreementType.TERMS_OF_SERVICE,
                "1.0",
                "Terms content",
                "abc"
        );
        LegalAgreement integrity = createAgreement(
                LegalAgreementType.ACADEMIC_INTEGRITY,
                "1.0",
                "Integrity content",
                "def"
        );

        when(agreementRepository.findActiveAgreementsForTenant(tenantId)).thenReturn(List.of(tos, integrity));

        StudentAgreementAcceptance acc = new StudentAgreementAcceptance(
                tenantId, userId, tos.getId(), "1.0", "127.0.0.1", "Mozilla/5.0"
        );
        acc.setAcceptedAt(Instant.now());
        when(acceptanceRepository.findByTenantIdAndStudentUserId(tenantId, userId)).thenReturn(List.of(acc));

        List<LegalAgreementDto> dtos = agreementService.getAgreementsForStudent(tenantId, userId);

        assertThat(dtos).hasSize(2);
        LegalAgreementDto tosDto = dtos.stream().filter(d -> d.agreementType() == LegalAgreementType.TERMS_OF_SERVICE).findFirst().orElseThrow();
        LegalAgreementDto integrityDto = dtos.stream().filter(d -> d.agreementType() == LegalAgreementType.ACADEMIC_INTEGRITY).findFirst().orElseThrow();

        assertThat(tosDto.isAccepted()).isTrue();
        assertThat(tosDto.acceptedAt()).isNotNull();
        assertThat(integrityDto.isAccepted()).isFalse();
        assertThat(integrityDto.acceptedAt()).isNull();
    }

    @Test
    @DisplayName("Accepts agreement and saves acceptance audit trail")
    void acceptsAgreementSuccessfully() {
        String content = "Terms content";
        String validHash = "e03a10fff7c14e283050a2cb234dd42a96381dd1f59ce0f5634ca3df7ea60bbc";
        LegalAgreement tos = createAgreement(LegalAgreementType.TERMS_OF_SERVICE, "1.0", content, validHash);

        when(agreementRepository.findById(tos.getId())).thenReturn(Optional.of(tos));
        when(acceptanceRepository.existsByTenantIdAndStudentUserIdAndAgreementIdAndAgreementVersion(
                tenantId, userId, tos.getId(), "1.0")).thenReturn(false);

        StudentAgreementAcceptance savedAcc = new StudentAgreementAcceptance(
                tenantId, userId, tos.getId(), "1.0", "192.168.1.1", "Chrome"
        );
        when(acceptanceRepository.save(any(StudentAgreementAcceptance.class))).thenReturn(savedAcc);

        LegalAgreementDto result = agreementService.acceptAgreement(
                tenantId, userId, tos.getId(), "1.0", "192.168.1.1", "Chrome"
        );

        assertThat(result.isAccepted()).isTrue();
        verify(acceptanceRepository, times(1)).save(any(StudentAgreementAcceptance.class));
    }

    @Test
    @DisplayName("Rejects agreement acceptance when content hash is tampered")
    void rejectsTamperedAgreement() {
        String content = "Terms content modified by attacker";
        String originalHash = "5e11d21124430f8fe620cbbe8a27d7a7837894d03d0eb6ba5a05b22b64005b63";
        LegalAgreement tos = createAgreement(LegalAgreementType.TERMS_OF_SERVICE, "1.0", content, originalHash);

        when(agreementRepository.findById(tos.getId())).thenReturn(Optional.of(tos));

        assertThatThrownBy(() -> agreementService.acceptAgreement(
                tenantId, userId, tos.getId(), "1.0", "192.168.1.1", "Chrome"
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("integrity check failed");

        verify(acceptanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Identifies missing mandatory agreements")
    void identifiesMissingAgreements() {
        LegalAgreement tos = createAgreement(LegalAgreementType.TERMS_OF_SERVICE, "1.0", "a", "h1");
        LegalAgreement integrity = createAgreement(LegalAgreementType.ACADEMIC_INTEGRITY, "1.0", "b", "h2");
        LegalAgreement conduct = createAgreement(LegalAgreementType.CODE_OF_CONDUCT, "1.0", "c", "h3");

        when(agreementRepository.findActiveAgreementsForTenant(tenantId)).thenReturn(List.of(tos, integrity, conduct));
        // User accepted only TOS
        StudentAgreementAcceptance acc = new StudentAgreementAcceptance(tenantId, userId, tos.getId(), "1.0", "ip", "ua");
        when(acceptanceRepository.findByTenantIdAndStudentUserId(tenantId, userId)).thenReturn(List.of(acc));

        List<String> missing = new ArrayList<>();
        boolean allAccepted = agreementService.areAllMandatoryAgreementsAccepted(tenantId, userId, missing);

        assertThat(allAccepted).isFalse();
        assertThat(missing).containsExactlyInAnyOrder("ACADEMIC_INTEGRITY", "CODE_OF_CONDUCT");
    }
}
