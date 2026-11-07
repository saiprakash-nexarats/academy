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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private static final String TERMS_CONTENT = "Terms content";
    private static final String TERMS_HASH = "e03a10fff7c14e283050a2cb234dd42a96381dd1f59ce0f5634ca3df7ea60bbc";

    @BeforeEach
    void setUp() {
        agreementService = new LegalAgreementService(agreementRepository, acceptanceRepository);
    }

    private LegalAgreement createAgreement(LegalAgreementType type, String version, String content, String hash, UUID agTenantId) {
        LegalAgreement ag = new LegalAgreement();
        ag.setId(UUID.randomUUID());
        ag.setTenantId(agTenantId);
        ag.setAgreementType(type);
        ag.setVersion(version);
        ag.setTitle("Test " + type.name());
        ag.setContent(content);
        ag.setContentHash(hash);
        ag.setActive(true);
        ag.setEffectiveDate(Instant.now().minus(1, ChronoUnit.DAYS));
        return ag;
    }

    @Test
    @DisplayName("Lists agreements prioritizing tenant-specific agreement over global agreement")
    void listsAgreementsPrioritizingTenantSpecific() {
        LegalAgreement globalTos = createAgreement(LegalAgreementType.TERMS_OF_SERVICE, "1.0", "Global TOS", "hash1", null);
        LegalAgreement tenantTos = createAgreement(LegalAgreementType.TERMS_OF_SERVICE, "1.1-tenant", "Tenant Custom TOS", "hash2", tenantId);

        when(agreementRepository.findActiveAgreementsForTenant(eq(tenantId), any(Instant.class)))
                .thenReturn(List.of(globalTos, tenantTos));
        when(acceptanceRepository.findByTenantIdAndStudentUserId(tenantId, userId)).thenReturn(List.of());

        List<LegalAgreementDto> dtos = agreementService.getAgreementsForStudent(tenantId, userId);

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).version()).isEqualTo("1.1-tenant");
    }

    @Test
    @DisplayName("Tenant Isolation: Rejects agreement belonging to a foreign tenant (403 Forbidden)")
    void rejectsForeignTenantAgreement() {
        UUID foreignTenantId = UUID.randomUUID();
        LegalAgreement foreignAg = createAgreement(LegalAgreementType.TERMS_OF_SERVICE, "1.0", TERMS_CONTENT, TERMS_HASH, foreignTenantId);

        when(agreementRepository.findById(foreignAg.getId())).thenReturn(Optional.of(foreignAg));

        assertThatThrownBy(() -> agreementService.acceptAgreement(tenantId, userId, foreignAg.getId(), "1.0", "127.0.0.1", "TestAgent"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Agreement does not belong to the authenticated tenant");
    }

    @Test
    @DisplayName("Rejects agreement that is marked inactive (400 Bad Request)")
    void rejectsInactiveAgreement() {
        LegalAgreement ag = createAgreement(LegalAgreementType.TERMS_OF_SERVICE, "1.0", TERMS_CONTENT, TERMS_HASH, tenantId);
        ag.setActive(false);

        when(agreementRepository.findById(ag.getId())).thenReturn(Optional.of(ag));

        assertThatThrownBy(() -> agreementService.acceptAgreement(tenantId, userId, ag.getId(), "1.0", "127.0.0.1", "TestAgent"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Agreement is no longer active");
    }

    @Test
    @DisplayName("Rejects agreement with future effective date (400 Bad Request)")
    void rejectsFutureEffectiveAgreement() {
        LegalAgreement ag = createAgreement(LegalAgreementType.TERMS_OF_SERVICE, "1.0", TERMS_CONTENT, TERMS_HASH, tenantId);
        ag.setEffectiveDate(Instant.now().plus(7, ChronoUnit.DAYS)); // Effective in the future

        when(agreementRepository.findById(ag.getId())).thenReturn(Optional.of(ag));

        assertThatThrownBy(() -> agreementService.acceptAgreement(tenantId, userId, ag.getId(), "1.0", "127.0.0.1", "TestAgent"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Agreement is not yet effective");
    }

    @Test
    @DisplayName("Idempotent acceptance preserves the original acceptedAt timestamp")
    void preservesOriginalTimestampOnIdempotentAcceptance() {
        LegalAgreement ag = createAgreement(LegalAgreementType.TERMS_OF_SERVICE, "1.0", TERMS_CONTENT, TERMS_HASH, tenantId);
        when(agreementRepository.findById(ag.getId())).thenReturn(Optional.of(ag));

        Instant originalTimestamp = Instant.now().minus(5, ChronoUnit.DAYS);
        StudentAgreementAcceptance existing = new StudentAgreementAcceptance(
                tenantId, userId, ag.getId(), "1.0", TERMS_HASH, "192.168.1.1", "Mozilla"
        );
        existing.setAcceptedAt(originalTimestamp);

        when(acceptanceRepository.findByTenantIdAndStudentUserIdAndAgreementIdAndAgreementVersion(
                tenantId, userId, ag.getId(), "1.0")).thenReturn(Optional.of(existing));

        LegalAgreementDto result = agreementService.acceptAgreement(tenantId, userId, ag.getId(), "1.0", "192.168.1.2", "NewBrowser");

        assertThat(result.isAccepted()).isTrue();
        assertThat(result.acceptedAt()).isEqualTo(originalTimestamp);
        verify(acceptanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Accepts agreement and records immutable content_hash in acceptance audit record")
    void recordsContentHashInAcceptanceAuditRecord() {
        LegalAgreement ag = createAgreement(LegalAgreementType.TERMS_OF_SERVICE, "1.0", TERMS_CONTENT, TERMS_HASH, tenantId);
        when(agreementRepository.findById(ag.getId())).thenReturn(Optional.of(ag));
        when(acceptanceRepository.findByTenantIdAndStudentUserIdAndAgreementIdAndAgreementVersion(
                tenantId, userId, ag.getId(), "1.0")).thenReturn(Optional.empty());

        when(acceptanceRepository.save(any(StudentAgreementAcceptance.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        agreementService.acceptAgreement(tenantId, userId, ag.getId(), "1.0", "10.0.0.1", "Chrome 120");

        ArgumentCaptor<StudentAgreementAcceptance> captor = ArgumentCaptor.forClass(StudentAgreementAcceptance.class);
        verify(acceptanceRepository).save(captor.capture());
        StudentAgreementAcceptance saved = captor.getValue();

        assertThat(saved.getContentHash()).isEqualTo(TERMS_HASH);
        assertThat(saved.getAgreementVersion()).isEqualTo("1.0");
        assertThat(saved.getIpAddress()).isEqualTo("10.0.0.1");
    }

    @Test
    @DisplayName("Rejects agreement acceptance if content hash fails integrity verification")
    void rejectsAlteredAgreementContent() {
        LegalAgreement ag = createAgreement(LegalAgreementType.TERMS_OF_SERVICE, "1.0", "Tampered content", TERMS_HASH, tenantId);
        when(agreementRepository.findById(ag.getId())).thenReturn(Optional.of(ag));

        assertThatThrownBy(() -> agreementService.acceptAgreement(tenantId, userId, ag.getId(), "1.0", "10.0.0.1", "Chrome"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cryptographic integrity check failed");
    }
}
