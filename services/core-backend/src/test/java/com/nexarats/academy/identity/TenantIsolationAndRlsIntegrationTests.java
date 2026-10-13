package com.nexarats.academy.identity;

import com.nexarats.academy.common.tenant.TenantContext;
import com.nexarats.academy.identity.dto.LoginRequest;
import com.nexarats.academy.identity.dto.LoginResponse;
import com.nexarats.academy.identity.dto.RegisterRequest;
import com.nexarats.academy.identity.model.Role;
import com.nexarats.academy.identity.model.TenantMembership;
import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.repository.RoleRepository;
import com.nexarats.academy.identity.repository.TenantMembershipRepository;
import com.nexarats.academy.identity.repository.UserRepository;
import com.nexarats.academy.identity.service.AuthService;
import com.nexarats.academy.tenant.Tenant;
import com.nexarats.academy.tenant.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
class TenantIsolationAndRlsIntegrationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantMembershipRepository membershipRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private com.nexarats.academy.identity.service.RateLimitingService rateLimitingService;

    private Tenant tenantA;
    private Tenant tenantB;
    private String emailUserA;
    private String rawPassword;
    private String testIp;

    @BeforeEach
    void setUp() {
        rateLimitingService.resetRateLimits();
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        tenantA = tenantService.createTenant("tenant-a-" + suffix, "Enterprise Client Alpha");
        tenantB = tenantService.createTenant("tenant-b-" + suffix, "Enterprise Client Beta");
        emailUserA = "user.a." + suffix + "@alpha.corp";
        rawPassword = "PasswordAlpha2026!";
        testIp = "10.0." + (int)(Math.random() * 200) + "." + (int)(Math.random() * 200);
    }

    @Test
    @DisplayName("Tenant Isolation: User in Tenant A cannot log in or switch to Tenant B without membership")
    void testCrossTenantAccessDenied() {
        // Register user under Tenant A
        RegisterRequest registerReq = new RegisterRequest(emailUserA, rawPassword, "Alpha", "User", null);
        LoginResponse registered = authService.register(registerReq, testIp, "TestAgent");
        User userA = userRepository.findById(registered.user().id()).orElseThrow();

        // Assign user exclusively to Tenant A
        TenantMembership memA = new TenantMembership(tenantA, userA, "ACTIVE");
        Role employeeRole = roleRepository.findByCode("ROLE_EMPLOYEE").orElseThrow();
        memA.addRole(employeeRole);
        membershipRepository.save(memA);

        // 1. Attempt login explicitly requesting Tenant B -> must fail
        LoginRequest loginTenantB = new LoginRequest(emailUserA, rawPassword, tenantB.getSlug(), "WEB");
        assertThatThrownBy(() -> authService.login(loginTenantB, testIp, "TestAgent"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not an active member");

        // 2. Attempt tenant switch to Tenant B without membership -> must fail
        assertThatThrownBy(() -> authService.switchTenant(userA.getId(), tenantB.getId(), testIp, "TestAgent"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not a member");
    }

    @Test
    @DisplayName("Tenant Switching: Multi-tenant user can cleanly switch among authorized active memberships")
    void testAuthorizedTenantSwitching() {
        RegisterRequest registerReq = new RegisterRequest(emailUserA, rawPassword, "Multi", "TenantUser", null);
        LoginResponse registered = authService.register(registerReq, testIp, "TestAgent");
        User user = userRepository.findById(registered.user().id()).orElseThrow();

        Role roleEmployee = roleRepository.findByCode("ROLE_EMPLOYEE").orElseThrow();
        Role roleMentor = roleRepository.findByCode("ROLE_MENTOR").orElseThrow();

        // Grant active membership in Tenant A with ROLE_EMPLOYEE
        TenantMembership memA = new TenantMembership(tenantA, user, "ACTIVE");
        memA.addRole(roleEmployee);
        membershipRepository.save(memA);

        // Grant active membership in Tenant B with ROLE_MENTOR
        TenantMembership memB = new TenantMembership(tenantB, user, "ACTIVE");
        memB.addRole(roleMentor);
        membershipRepository.save(memB);

        // Switch to Tenant A
        LoginResponse switchedA = authService.switchTenant(user.getId(), tenantA.getId(), testIp, "TestAgent");
        assertThat(switchedA.tenant().id()).isEqualTo(tenantA.getId());
        assertThat(switchedA.roles()).contains("ROLE_EMPLOYEE");

        // Switch to Tenant B
        LoginResponse switchedB = authService.switchTenant(user.getId(), tenantB.getId(), testIp, "TestAgent");
        assertThat(switchedB.tenant().id()).isEqualTo(tenantB.getId());
        assertThat(switchedB.roles()).contains("ROLE_MENTOR");
    }

    @Test
    @DisplayName("Suspended Membership: Access is rejected when membership status is SUSPENDED")
    void testSuspendedMembershipRejected() {
        RegisterRequest registerReq = new RegisterRequest(emailUserA, rawPassword, "Suspended", "User", null);
        LoginResponse registered = authService.register(registerReq, testIp, "TestAgent");
        User user = userRepository.findById(registered.user().id()).orElseThrow();

        // Create membership with SUSPENDED status
        TenantMembership memA = new TenantMembership(tenantA, user, "SUSPENDED");
        Role studentRole = roleRepository.findByCode("ROLE_STUDENT").orElseThrow();
        memA.addRole(studentRole);
        membershipRepository.save(memA);

        LoginRequest loginReq = new LoginRequest(emailUserA, rawPassword, tenantA.getSlug(), "WEB");
        assertThatThrownBy(() -> authService.login(loginReq, testIp, "TestAgent"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("suspended or inactive");
    }

    @Test
    @DisplayName("PostgreSQL RLS: Row-Level Security restricts query visibility strictly to active tenant context")
    void testPostgreSqlRowLevelSecurity() {
        // Register users and assign to Tenant A
        RegisterRequest userReqA = new RegisterRequest(emailUserA, rawPassword, "AlphaUser", "RLS", null);
        LoginResponse loginA = authService.register(userReqA, testIp, "TestAgent");
        User userA = userRepository.findById(loginA.user().id()).orElseThrow();

        TenantMembership memA = new TenantMembership(tenantA, userA, "ACTIVE");
        Role roleEmployee = roleRepository.findByCode("ROLE_EMPLOYEE").orElseThrow();
        memA.addRole(roleEmployee);
        membershipRepository.save(memA);

        // Create an explicit active session in Tenant A
        authService.switchTenant(userA.getId(), tenantA.getId(), testIp, "TestAgent");

        // Verify database RLS policy on user_sessions:
        // Must be transaction-scoped on a single JDBC connection so SET LOCAL remains active.
        jdbcTemplate.execute((java.sql.Connection con) -> {
            boolean origAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try (java.sql.Statement stmt = con.createStatement()) {
                // 1. When tenant context is set to Tenant A, Tenant A records are visible
                stmt.execute("SET LOCAL ROLE academy_app_user");
                stmt.execute("SET LOCAL app.bypass_rls = 'false'");
                stmt.execute("SET LOCAL app.current_tenant_id = '" + tenantA.getId() + "'");

                try (java.sql.ResultSet rs = stmt.executeQuery("SELECT count(*) FROM user_sessions")) {
                    rs.next();
                    int visibleToA = rs.getInt(1);
                    assertThat(visibleToA).isGreaterThan(0);
                }

                // 2. When tenant context is set to Tenant B, Tenant A records are invisible
                stmt.execute("SET LOCAL app.current_tenant_id = '" + tenantB.getId() + "'");
                try (java.sql.ResultSet rs = stmt.executeQuery("SELECT count(*) FROM user_sessions WHERE tenant_id = '" + tenantA.getId() + "'")) {
                    rs.next();
                    int visibleCrossTenant = rs.getInt(1);
                    assertThat(visibleCrossTenant).isEqualTo(0);
                }

                // 3. When tenant context is set to a tenant with no sessions, fail closed (0 rows)
                stmt.execute("SET LOCAL app.current_tenant_id = '" + UUID.randomUUID() + "'");
                try (java.sql.ResultSet rs = stmt.executeQuery("SELECT count(*) FROM user_sessions")) {
                    rs.next();
                    int failClosed = rs.getInt(1);
                    assertThat(failClosed).isEqualTo(0);
                }
            } finally {
                con.rollback();
                con.setAutoCommit(origAutoCommit);
            }
            return null;
        });
    }
}
