package com.nexarats.academy.onboarding.repository;

import com.nexarats.academy.onboarding.model.AvatarUploadTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvatarUploadTicketRepository extends JpaRepository<AvatarUploadTicket, UUID> {

    Optional<AvatarUploadTicket> findByQuarantineKey(String quarantineKey);

    Optional<AvatarUploadTicket> findByQuarantineKeyAndUserId(String quarantineKey, UUID userId);

    Optional<AvatarUploadTicket> findByQuarantineKeyAndTenantIdAndUserId(String quarantineKey, UUID tenantId, UUID userId);

    List<AvatarUploadTicket> findByUserIdAndStatus(UUID userId, String status);

    List<AvatarUploadTicket> findByTenantIdAndUserId(UUID tenantId, UUID userId);
}
