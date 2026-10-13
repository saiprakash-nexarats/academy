# Nexarats Academy — Phase 1 Architecture Decision Records (ADRs)
## Foundational Architectural Decisions & Phase 0 Corrections

**Document ID:** `15_PHASE_1_ARCHITECTURE_DECISIONS.md`  
**Phase:** Phase 1 (Enterprise Monorepo Foundation, Java Backend Skeleton, and Local Development Infrastructure)  
**Status:** Approved & Implemented  
**Date:** 2026-10-13  

---

## ADR-001: Adoption of pnpm Workspaces and Turborepo for Frontend Monorepo

### Context
Nexarats Academy requires an enterprise monorepo capable of hosting multiple frontend applications (`apps/web`, `apps/mobile`), shared packages (`packages/ui-tokens`, `packages/api-contracts`), and supporting polyglot backend services. The existing React application needed to be relocated without breaking build pipelines, asset resolutions, or developer workflows.

### Decision
1. Adopt **pnpm workspaces** (`pnpm-workspace.yaml`) for dependency isolation, fast disk caching via hard links, and reproducible workspace builds.
2. Adopt **Turborepo** (`turbo.json`) as the monorepo build orchestrator for TypeScript packages.
3. Keep the Java Spring Boot backend under native **Maven** management, explicitly integrated into CI workflows without artificially routing Java compilation through Turborepo.

### Consequences
- `@nexarats/web` resides cleanly in `apps/web/`.
- `@nexarats/ui-tokens` compiles independently to `dist/` and can be shared across web and mobile.
- Polyglot tooling boundaries are clean and unambiguous.

---

## ADR-002: Upgrade to Spring Boot 4.1.1 and Spring Modulith 2.1.1 on Java 21 LTS

### Context
Phase 0 documentation referenced Spring Boot 3.3. However, Spring Boot 3.3 has reached open-source support expiration (EOL) and is no longer actively supported for enterprise security patches. Furthermore, Java 21 LTS is the authoritative enterprise LTS baseline.

### Decision
1. Standardize on **Java 21 LTS** (`Microsoft OpenJDK 21.0.12.1` in development, Eclipse Temurin in CI).
2. Adopt **Spring Boot 4.1.1** as the active supported enterprise foundation.
3. Adopt **Spring Modulith 2.1.1** for domain module encapsulation, dependency verification, and domain boundary enforcement.

### Consequences
- Architectural tests in `ModularityTests.java` verify package boundaries across all 13 modules (`common`, `identity`, `tenant`, `academic`, `admissions`, `cohort`, `mentorship`, `assessment`, `learning`, `corporate`, `finance`, `notification`, `audit`).
- Resolves security vulnerability exposure from out-of-support Spring lines.

---

## ADR-003: Deprecation and Archival of Prototype Node/Express Skeleton

### Context
The repository root contained `server/package.json` and `server/tsconfig.json` representing an incomplete Node/Express/Prisma skeleton. Having multiple competing backend skeletons introduces architectural drift and developer confusion.

### Decision
1. Relocate the unused configuration files to `legacy/server-node-skeleton/`.
2. Do not install dependencies, generate Prisma clients, or bind ports to this directory.
3. Provide a provenance `README.md` documenting historical context and marking it deprecated.
4. Establish `services/core-backend/` as the single source of truth for the platform backend.

### Consequences
- Complete architectural clarity.
- No wasted memory or conflicting port bindings.

---

## ADR-004: Defense-in-Depth Multi-Tenancy and Database Security

### Context
Phase 0 documentation contained references suggesting database Row Level Security (RLS) alone could neutralize tenant leaks and SQL injection risks.

### Decision
1. **Tenant Context Resolution:** Client-provided tenant headers (`X-Tenant-ID`) or subdomain parameters must NEVER be trusted blindly. Tenant identity must be resolved strictly through authenticated server-side sessions, cryptographic JWT claims, or verified organization domain bindings.
2. **PostgreSQL RLS Role:** PostgreSQL RLS is classified strictly as **defense-in-depth**, not as a replacement for application authorization, parameterized queries, or ORM scoping.
3. **Thread Safety:** `TenantContext` uses thread-local scoping with mandatory cleanup in servlet filters to prevent thread-pool context leakage.

### Consequences
- Complete immunity against unauthenticated tenant spoofing.
- Explicit compliance with OWASP Top 10 API Security guidelines.

---

## ADR-005: Screen Count Reconciliation (Correction of Typo in Phase 0 Summary)

### Context
Phase 0 documentation contained an internal numerical mismatch:
- `08_WEB_AND_MOBILE_SCREEN_INVENTORY.md` categorical sum = **215 screens**
- `00_PHASE_0_AUDIT_REPORT.md` executive summary = **202 screens**

### Decision
Audit the full screen inventory item-by-item:
- Category 1: Public Web Portal = **8 screens** (Home, Programs, Program Detail, Courses, Mentors, Stories, About, Contact)
- Category 2: Student Experience = **30 screens**
- Category 3: Mentor Experience = **20 screens**
- Category 4: Academy Administrator = **35 screens**
- Category 5: Corporate Administrator = **25 screens**
- Category 6: Corporate Employee = **20 screens**
- Category 7: Parent / Guardian = **10 screens**
- Category 8: Platform Super Administrator = **25 screens**
- Category 9: Native Mobile Application = **42 screens**
**Authoritative Grand Total = 215 screens.**

### Correction
The summary figure of 202 in Phase 0 was an unintentional subtraction error during draft aggregation. The authoritative number for the 70-phase platform is **215 screens**.

---

## ADR-006: Disclaimer on Accreditations, Regulatory Approvals, and Employment Claims

### Context
Landing page content and prototype copy contained unverified statistical claims regarding student hiring percentages ("94% placement rate") and institutional accreditations.

### Decision
1. Flag all marketing statistics and accreditation claims in documentation as **subject to legal and compliance review**.
2. Preserve current landing page text during Phase 1 to satisfy the absolute visual and behavioral preservation directive.
3. In Phase 2 and subsequent administrative phases, require dynamic configuration of institutional claims with audit trails and legal review flags.

---

## ADR-007: Official Adoption of Flutter and Dart for Mobile Applications (Android & iOS)

### Context
Phase 0 architectural drafts and initial monorepo scaffolding planned cross-platform mobile development using React Native and Expo SDK. Upon executive architectural review, Flutter + Dart was selected as the superior enterprise mobile technology for Nexarats Academy to deliver high-performance native rendering, consistent canvas-level typography rendering, unified WebRTC media pipeline integration with LiveKit, and strong type safety through Dart.

### Decision
1. **Officially Adopt Flutter + Dart** as the exclusive mobile technology for Android and iOS (`apps/mobile/`).
2. **Supersede React Native & Expo:** Mark all prior references to React Native, Expo SDK, Metro Bundler, React Navigation, NativeWind, and EAS Build as superseded.
3. **Preserve React Web:** The public website and web applications remain built with React 19 + TypeScript + Vite. Flutter is exclusively for the mobile applications.
4. **Decoupled API Consumption:** React Web will use a TypeScript API client, and Flutter will use a Dart API client, both generated from the same authoritative Spring Boot OpenAPI specification.
5. **Architectural Framework:**
   - State Management: `flutter_bloc`
   - Declarative Navigation: `go_router`
   - Networking & Interceptors: `dio`
   - Local Offline Persistence: `drift` (SQLite)
   - Secure Hardware Key Storage: `flutter_secure_storage`
   - Realtime WebRTC Live Classroom: `livekit_client`

### Consequences
- Eliminates JavaScript bridge overhead and cross-platform styling inconsistencies on mobile.
- `apps/mobile/` is completely decoupled from Node.js runtime dependencies.
- Mobile screen implementation (42 screens) will commence in Phase 53 as planned.

---

## ADR-008: Comprehensive Security Boundaries and Strict Defense-in-Depth

### Context
Previous documents made generalized security assertions that could be misinterpreted as absolute guarantees (e.g., claiming database Row-Level Security alone eliminates SQL injection or data leaks).

### Decision
Codify 15 mandatory security axioms across the platform:
1. **PostgreSQL RLS is Defense-in-Depth:** RLS protects against accidental multi-tenant cross-contamination; it does not replace secure parameterized queries, input validation, or application authorization.
2. **Untrusted Client Tenant Headers:** Tenant identifiers supplied by clients (`X-Tenant-ID`, path parameters, or subdomains) must NEVER be trusted without cryptographic validation and server-side authorization.
3. **Server-Side Context Derivation:** Tenant context must be resolved strictly on the server (via validated JWT claims or verified DNS domain bindings).
4. **Connection Pool Scoping:** Connection pooling (HikariCP) requires transaction-scoped tenant isolation (`SET LOCAL app.current_tenant_id`) with mandatory resetting upon connection return.
5. **Thread-Local Cleanliness:** `TenantContext` must be cleared in a `finally` block in servlet filters/interceptors to prevent thread-pool reuse leaks.
6. **Multi-Organization Membership:** Users belonging to multiple tenant organizations must select an active tenant session; permissions do not cross tenant boundaries.
7. **Authoritative Server Enforcement:** All authentication, authorization, and permission checks must be executed and enforced on the server. Client UI state is advisory only.
8. **CSRF Mitigation:** Cookie-based session authentication requires SameSite cookies and cryptographically signed anti-CSRF tokens.
9. **Zero Secret Logging:** Passwords, tokens, API keys, and PII must never appear in application logs or exception messages.
10. **Mobile Secure Storage:** Mobile credentials must reside in platform hardware security modules (`flutter_secure_storage`: Android Keystore / iOS Keychain).
11. **Comprehensive JWT Verification:** JWT validation must verify signature, issuer (`iss`), audience (`aud`), expiration (`exp`), and required subject/tenant claims.
12. **Server-Side Session Revocation:** Token blacklisting and account suspension must be checked server-side via Redis/database on every sensitive operation.
13. **AI Tenant Sandboxing:** AI subsystem requests must include verified tenant context and must never query vector stores across tenant partitions.
14. **Time-Limited Signed Media URLs:** S3/MinIO media URLs must be pre-signed with short expiration and scoped strictly to authorized enrolled students.
15. **Explicit Environment Security:** Development defaults (such as local test bypasses) must never be active in production configurations.

