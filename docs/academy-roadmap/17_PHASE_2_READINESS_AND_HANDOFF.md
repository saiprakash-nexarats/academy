# Nexarats Academy — Phase 2 Readiness and Handoff Specification
## Handoff from Phase 1 Foundation to Phase 2 Authentication & Identity

**Document ID:** `17_PHASE_2_READINESS_AND_HANDOFF.md`  
**From:** Phase 1 (Enterprise Monorepo Foundation, Java Backend Skeleton, and Local Development Infrastructure)  
**To:** Phase 2 (Enterprise Identity, Authentication, Multi-Tenant Session Resolution, and RBAC)  
**Date:** 2026-10-13  
**Status:** Ready for Phase 2 Authorization  

---

## 1. Readiness Assessment Matrix

| Foundation Dimension | Phase 1 Status | Hand-off State to Phase 2 |
|---|---|---|
| **Monorepo Architecture** | Complete | `apps/web`, `packages/ui-tokens`, `packages/api-contracts` ready for auth components |
| **Landing Page Preservation** | 100% Intact | All 8 public routes, mock courses, and UI intact. No premature fake auth wired |
| **Java 21 Backend Runtime** | Compiling & Verified | Spring Boot 4.1.1 + Spring Modulith 2.1.1 passes all context & module tests |
| **Security Pipeline Foundation** | Configured | Stateless `SecurityFilterChain` permitting `/api/v1/health` and `/actuator/health` |
| **Error Handling & Observability** | Configured | RFC 7807 `ProblemDetail` handler + `CorrelationIdFilter` (`X-Correlation-ID`) |
| **Multi-Tenancy Foundation** | Scaffolding Ready | `TenantContext` thread-local created with strict server-side validation policy |
| **Database Migration Engine** | Configured | Flyway V1 migration prepared (`tenants` table baseline) |
| **Local Infrastructure** | Configured | Docker Compose for PostgreSQL 16, Redis 7, and MinIO ready |
| **CI/CD Pipeline** | Configured | GitHub Actions `.github/workflows/ci.yml` matrix ready |

---

## 2. Phase 2 Scope & Architectural Directives

When Phase 2 is authorized, the engineering team will execute:

### 2.1 Identity & Access Management Module (`com.nexarats.academy.identity`)
1. Implement JPA entities: `User`, `UserCredential`, `Role`, `Permission`, `UserSession`.
2. Implement password hashing using **Argon2id** (OWASP recommended parameters).
3. Implement dual-token JWT lifecycle:
   - Access token (short-lived, 15 min, cryptographically signed with RS256 or Ed25519).
   - Refresh token (opaque, stored in Redis with sliding expiration and single-use rotation).
4. Implement token revocation / blacklisting via Redis.

### 2.2 Multi-Tenant Context Resolution (`com.nexarats.academy.tenant`)
1. Enforce ADR-004: Tenant context must NEVER be accepted blindly from client headers or parameters.
2. Resolve tenant context server-side:
   - For public domain visits: verified subdomain DNS lookup against `tenants.slug`.
   - For authenticated users: verified `tenant_id` claim inside authenticated JWT.
3. Bind active tenant ID to `TenantContext` during request lifecycle and clear upon request completion.

### 2.3 Role-Based Access Control (RBAC) Engine
1. Implement the 8 core platform roles:
   - `STUDENT`
   - `MENTOR`
   - `ACADEMY_ADMIN`
   - `CORPORATE_ADMIN`
   - `EMPLOYEE`
   - `GUARDIAN`
   - `SUPER_ADMIN`
   - `ANONYMOUS`
2. Enforce method-level security with `@PreAuthorize("hasRole(...)")`.

### 2.4 Frontend Auth Integration (`apps/web`)
1. Implement `AuthContext` and secure local session handling.
2. Build login, register, and password-reset modal flows preserving current visual design.
3. Protect future dashboard routes while leaving public landing routes completely accessible.

---

## 3. Strict Pre-Phase 2 Constraint

**CRITICAL NOTICE:** Phase 2 implementation must NOT begin without explicit user instruction. No authentication endpoints, user registration controllers, or token generators may be created until Phase 1 is formally reviewed and accepted.
