# Nexarats Academy — Phase 3 Readiness and Handoff Specification
## Handoff from Phase 2 Identity Foundation to Phase 3 Public Catalog & Dynamic Programs API

**Document Reference:** `docs/academy-roadmap/26_PHASE_3_READINESS_AND_HANDOFF.md`  
**Execution Agent:** Gemini 3.8 Flash (High) — Antigravity  
**From:** Phase 2 (Enterprise Identity, Authentication, Multi-Tenant Session Resolution, and RBAC)  
**To:** Phase 3 (Public Catalog & Dynamic Programs API)  
**Target Platform:** Nexarats Academy Enterprise Education Platform  
**Authoritative Roadmap:** 70 Phases (Milestone 1: Core Foundation & Minimum Usable Student Learning MVP)  
**Date:** October 13, 2026  
**Status:** PHASE 2 COMPLETE — READY FOR PHASE 3 AUTHORIZATION  

---

## 1. Phase 2 Final Acceptance Assessment

Phase 2 has fulfilled 100% of its required objectives with zero blocking issues. The platform now possesses a verified, enterprise-ready authentication, multi-tenant session, and role-based access control engine.

| Acceptance Criterion | Verification Method | Result | Status |
|---|---|---|---|
| **1. Flyway Schema Governance** | `spring.jpa.hibernate.ddl-auto=validate` checked across Spring Modulith event registry and 11 identity tables | Migrations `V1`–`V6` verified in PostgreSQL 16.8; Hibernate validation clean | **ACCEPTED** |
| **2. Cryptographic Passwords** | Argon2id hashing with $m=64\text{MB}, t=3, p=1$ via BouncyCastle; constant-time matching | Unit & integration tests; dummy hash for unknown users | **ACCEPTED** |
| **3. RS256 JWT Access Tokens** | 2048-bit asymmetric RSA key pairs, 15-min lifetime, user/tenant/role claims | Token issuance & verification tested; wrong issuer/signature rejected | **ACCEPTED** |
| **4. Opaque Refresh Token Rotation** | 64-char cryptographically random tokens, SHA-256 hashed in DB & Redis, single-use | Rotation tested; reuse/replay triggers immediate session family revocation | **ACCEPTED** |
| **5. Session Control & Redis Cache** | Redis 7.4 active session cache, sliding-window rate limiters, memory fallback | Sub-millisecond revocation and IP/account throttling verified | **ACCEPTED** |
| **6. Multi-Tenant Context & RLS** | Server-side validation of tenant membership; PostgreSQL Row-Level Security on `user_sessions` | Cross-tenant access denied; RLS verified under `academy_app_user` | **ACCEPTED** |
| **7. 11 Authoritative Roles & RBAC** | All 11 roles from `04_USER_ROLES_AND_USER_JOURNEYS.md` seeded in DB; method-level `@PreAuthorize` | RBAC test suite passed: Student, Admin, Super Admin isolation verified | **ACCEPTED** |
| **8. Web Integration & Preservation** | `LoginModal.tsx` and `Header.tsx` connected to live API; visual design 100% preserved | `pnpm run lint:web` (0 errors) & `pnpm run build:web` clean production build | **ACCEPTED** |
| **9. Shared API Contracts** | `@nexarats/api-contracts` package created with TypeScript DTOs and OpenAPI 3.1 YAML | Type-safe imports in web application; OpenAPI doc accessible | **ACCEPTED** |
| **10. Mobile Session Architecture** | Native Flutter credential storage, Dio interceptor, and tenant switching specified | `apps/mobile/README.md` updated with Section 5 | **ACCEPTED** |

---

## 2. Reusable Authentication APIs for Future Phases

Future phases (starting with Phase 3 catalog authoring, and later Phase 4–12 LMS features) can leverage the following operational backend APIs and security annotations:

### 2.1 Backend Security Infrastructure
- **Security Context:** Any controller can access the authenticated user via `@AuthenticationPrincipal UserPrincipal principal`.
- **Tenant Context:** Any service or query can inspect the active tenant via `TenantContext.getCurrentTenantId()`.
- **Method Security:** Guard endpoints using `@PreAuthorize("hasAuthority('ROLE_STUDENT')")` or `@PreAuthorize("hasAnyAuthority('ROLE_ACADEMY_ADMIN', 'ROLE_SUPER_ADMIN')")`.
- **Row-Level Security:** For tenant-owned tables, follow the pattern established in `V3` and `V6` using `app.current_tenant_id` session settings.

### 2.2 Operational Authentication Endpoints

| Endpoint | Method | Security | Purpose |
|---|---|---|---|
| `/api/v1/auth/login` | `POST` | Public (Rate-limited) | Authenticates email/password; returns access token + sets HttpOnly cookie |
| `/api/v1/auth/register` | `POST` | Public (Rate-limited) | Registers learner into default academy tenant |
| `/api/v1/auth/refresh` | `POST` | Cookie + CSRF | Rotates opaque refresh token; issues new access token |
| `/api/v1/auth/me` | `GET` | Bearer Token | Returns current authenticated profile, active tenant, and assigned roles |
| `/api/v1/auth/logout` | `POST` | Cookie + CSRF or Bearer | Revokes session in DB and Redis; clears cookies |
| `/api/v1/auth/switch-tenant/{tenantId}` | `POST` | Bearer Token | Switches active tenant context if user holds active membership |
| `/api/v1/auth/change-password` | `POST` | Bearer Token | Verifies old password, updates hash, revokes other sessions |
| `/api/v1/auth/forgot-password` | `POST` | Public (Rate-limited) | Generates secure password reset token (captured by `EmailService`) |
| `/api/v1/auth/reset-password` | `POST` | Public (Rate-limited) | Verifies token hash, updates password, revokes previous sessions |

---

## 3. Database Schema Baseline & Seed Constraints for Phase 3

### 3.1 Existing Applied Migrations
1. `V1__init_tenant_schema.sql` — `tenants` table with status, slug, name, and timestamps.
2. `V2__spring_modulith_event_publication.sql` — Spring Modulith JPA Event Publication registry.
3. `V3__identity_and_rbac_schema.sql` — Tables: `users`, `user_credentials`, `tenant_memberships`, `roles`, `permissions`, `membership_roles`, `role_permissions`, `user_sessions`, `refresh_tokens`, `password_reset_tokens`, `email_verification_tokens`, `auth_audit_events`.
4. `V4__seed_roles_and_permissions.sql` — 11 seeded system roles (`ROLE_STUDENT`, `ROLE_EMPLOYEE`, `ROLE_MENTOR`, `ROLE_TA`, `ROLE_COUNSELOR`, `ROLE_EXAMINER`, `ROLE_FINANCE`, `ROLE_ACADEMY_ADMIN`, `ROLE_CORP_ADMIN`, `ROLE_GUARDIAN`, `ROLE_SUPER_ADMIN`).
5. `V5__relax_audit_foreign_key.sql` — Unauthenticated audit log support.
6. `V6__application_db_role.sql` — Provisioned `academy_app_user` for non-superuser RLS enforcement.

### 3.2 Guidelines for Phase 3 Migrations
- The next migration MUST be `V7__create_programs_schema.sql`.
- Do NOT modify migrations `V1` through `V6`.
- Tables created in Phase 3 (`programs`, `program_categories`, etc.) must link to `tenants(id)` if programs are tenant-scoped, or be marked platform-wide if global.
- Maintain `spring.jpa.hibernate.ddl-auto=validate`.

---

## 4. Current Frontend Session & State Management

In `apps/web`:
- **Auth Provider:** `apps/web/src/context/AuthContext.tsx` wraps the application in `App.tsx`.
- **In-Memory Token:** JWT access tokens are stored in React state memory; tokens are never persisted in `localStorage` or `sessionStorage`.
- **Silent Restoration:** On browser reload, `AuthContext` calls `POST /api/v1/auth/refresh` with credentials included; if the HttpOnly cookie is present, the session is silently restored.
- **Header State:** `Header.tsx` detects `isAuthenticated`:
  - When authenticated: Displays a blue badge with learner avatar/initials, first name, and a "Sign Out" dropdown button.
  - When unauthenticated: Displays the standard "Sign In" button opening `LoginModal.tsx`.
- **Design Preservation:** The public landing page, courses page, programs page, animations, typography, and responsive styles remain 100% untouched.

---

## 5. Phase 3 Scope & Prerequisites

### 5.1 Authoritative Phase 3 Objective
**Phase 3: Public Catalog & Dynamic Programs API**
- Migrate static program data from `apps/web/src/data/programs.ts` to dynamic PostgreSQL database.
- Expose REST endpoints for programs, categories, and duration filtering under `/api/v1/programs`.
- Implement `ProgramService` and `ProgramRepository` with Redis caching.
- Connect `ProgramsPage.tsx` and `PopularProgramsSection.tsx` via TanStack Query with loading skeletons while preserving exact visual appearance.

### 5.2 Phase 3 Prerequisites (Satisfied by Phase 2)
1. **PostgreSQL 16.8 & Redis 7.4:** Running and healthy.
2. **Flyway Migration Engine:** Ready for `V7` script.
3. **Hibernate Schema Validation:** In active `validate` mode.
4. **Spring Modulith Boundaries:** Ready for `com.nexarats.academy.catalog` or `program` module.
5. **Vite Proxy & API Client:** Configured to proxy `/api` calls directly to backend on port 8085.

---

## 6. Strict Phase Boundary Protocol

**CRITICAL NOTICE:** In accordance with prompt instructions:
- **Phase 2 is fully complete.**
- **Phase 3 must NOT be started automatically.**
- All development stops here pending user authorization.
