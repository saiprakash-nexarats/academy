package com.nexarats.academy.onboarding.repository;

import com.nexarats.academy.onboarding.model.StudentAgreementAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentAgreementAcceptanceRepository extends JpaRepository<StudentAgreementAcceptance, UUID> {

    List<StudentAgreementAcceptance> findByTenantIdAndStudentUserId(UUID tenantId, UUID studentUserId);

    List<StudentAgreementAcceptance> findByStudentUserId(UUID studentUserId);

    Optional<StudentAgreementAcceptance> findByTenantIdAndStudentUserIdAndAgreementIdAndAgreementVersion(
            UUID tenantId, UUID studentUserId, UUID agreementId, String agreementVersion);

    boolean existsByTenantIdAndStudentUserIdAndAgreementIdAndAgreementVersion(
            UUID tenantId, UUID studentUserId, UUID agreementId, String agreementVersion);

    boolean existsByStudentUserIdAndAgreementIdAndAgreementVersion(
            UUID studentUserId, UUID agreementId, String agreementVersion);
}
