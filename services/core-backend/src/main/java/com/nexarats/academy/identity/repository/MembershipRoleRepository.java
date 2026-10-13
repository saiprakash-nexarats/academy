package com.nexarats.academy.identity.repository;

import com.nexarats.academy.identity.model.MembershipRole;
import com.nexarats.academy.identity.model.TenantMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MembershipRoleRepository extends JpaRepository<MembershipRole, UUID> {
    List<MembershipRole> findByMembership(TenantMembership membership);
    void deleteByMembership(TenantMembership membership);
}
