package com.nexarats.academy.onboarding.repository;

import com.nexarats.academy.onboarding.model.LegalAgreement;
import com.nexarats.academy.onboarding.model.LegalAgreementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LegalAgreementRepository extends JpaRepository<LegalAgreement, UUID> {

    @Query("SELECT la FROM LegalAgreement la WHERE la.isActive = true " +
            "AND (la.effectiveDate IS NULL OR la.effectiveDate <= :now) " +
            "AND (la.expirationDate IS NULL OR la.expirationDate > :now) " +
            "AND (:tenantId IS NULL OR la.tenantId = :tenantId OR la.tenantId IS NULL) " +
            "ORDER BY la.agreementType ASC, la.effectiveDate DESC")
    List<LegalAgreement> findActiveAgreementsForTenant(@Param("tenantId") UUID tenantId, @Param("now") Instant now);

    default List<LegalAgreement> findActiveAgreementsForTenant(UUID tenantId) {
        return findActiveAgreementsForTenant(tenantId, Instant.now());
    }

    List<LegalAgreement> findByIsActiveTrue();

    Optional<LegalAgreement> findByAgreementTypeAndVersion(LegalAgreementType agreementType, String version);

    Optional<LegalAgreement> findFirstByAgreementTypeAndIsActiveTrueOrderByEffectiveDateDesc(LegalAgreementType agreementType);
}
