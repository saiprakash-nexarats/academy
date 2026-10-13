# NEXARATS ACADEMY — SECURITY, PRIVACY & TENANT MODEL
## Enterprise Threat Modeling, Cryptographic Authentication, RBAC/ABAC, and Regulatory Compliance

- **Document ID:** `10_SECURITY_PRIVACY_AND_TENANT_MODEL`
- **Execution Date:** 2026-10-13
- **Classification:** Enterprise Security Architecture & Compliance Standard

---

## 1. Enterprise Multi-Tenant Isolation & Threat Model

### 1.1 Tenant Isolation Architecture
Nexarats Academy enforces isolation across three distinct tiers:

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        TIER 1: EDGE & NETWORK LAYER                     │
│  - Subdomain routing: {tenant}.nexarats.academy                        │
│  - Cloudflare WAF: Tenant rate-limiting & IP allowlisting               │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                    TIER 2: APPLICATION RUNTIME LAYER                   │
│  - Spring Security SecurityContextHolder scopes every request          │
│  - TenantContextFilter extracts tenant_id from verified JWT claims     │
│  - Zero trust: Service layer rejects queries lacking valid tenant_id   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                       TIER 3: DATA PERSISTENCE LAYER                   │
│  - PostgreSQL Row-Level Security (RLS) on all tenant-owned tables      │
│  - SET LOCAL app.current_tenant_id = '{uuid}' per database transaction │
│  - Redis keys prefixed with tenant namespace: tenant:{id}:...          │
│  - S3 bucket prefixes isolated: s3://nexarats-data/{tenant_id}/...     │
└────────────────────────────────────────────────────────────────────────┘
```

### 1.2 STRIDE Threat Model & Mitigations
| Threat Category | Potential Attack Vector | Nexarats Academy Architectural Defense |
| :--- | :--- | :--- |
| **Spoofing** | Forged JWT or impersonating a student or mentor | Asymmetric RS256 token signatures with private keys stored in AWS KMS; automated key rotation every 30 days. |
| **Tampering** | Altering grades or modifying certificate completion hashes | SHA-256 cryptographic hashes on all issued certificates; immutable append-only audit trail in PostgreSQL. |
| **Repudiation** | Instructor denies grading or student denies taking proctored exam | Signed event logs recording user ID, timestamp, IP address, and webcam proctoring session snapshots. |
| **Information Disclosure** | Multi-tenant cross-contamination (Tenant A accessing Tenant B's employee records) | Defense-in-depth: Server-side tenant scoping combined with PostgreSQL Row-Level Security (RLS) policies (`SET LOCAL app.current_tenant_id`). RLS provides secondary defense; parameterized queries, strict ORM scoping, and authorization checks remain primary defenses against data leakage and injection. |
| **Denial of Service** | Flooding API with automated script requests or brute-forcing login | Redis-backed distributed Token Bucket rate limiting: 100 req/min for general API, 5 req/min for `/login`. Cloudflare WAF DDoS mitigation. |
| **Elevation of Privilege** | Student modifying payload to grant themselves `ROLE_ADMIN` | RBAC enforced via Spring Security `@PreAuthorize` method annotations; role claims verified against active DB session on sensitive mutations. |

---

## 2. Authentication Architecture

### 2.1 Asymmetric Token Governance (RS256)
- **Access Tokens:** Short-lived (15 minutes). Formatted as standard JWT signed with RSA-2048 private key. Contains user ID, email, tenant ID, and granted authorities.
- **Refresh Tokens:** Long-lived (7 days). Stored in an encrypted `HttpOnly`, `Secure`, `SameSite=Strict` cookie. The backend persists the refresh token hash in Redis alongside client device fingerprints (User-Agent, IP hash).
- **Token Rotation & Revocation:** Every refresh token exchange issues a *new* refresh token and invalidates the previous one immediately. If a revoked refresh token is re-submitted, Redis triggers an immediate session compromise alert, revoking all active sessions for that user.

### 2.2 Enterprise Single Sign-On (SSO) & SCIM
- **Protocols Supported:** SAML 2.0 and OpenID Connect (OIDC).
- **Identity Providers (IdP):** Pre-built connectors for Okta, Microsoft Azure AD / Entra ID, Google Workspace, and PingIdentity.
- **Automated SCIM 2.0 Provisioning:** When an enterprise HR team onboard or offboard an employee in Okta, SCIM webhooks automatically provision or deactivate their Nexarats seat in real time.

### 2.3 Multi-Factor Authentication (MFA)
- Time-based One-Time Password (TOTP) compliant with RFC 6238 (Google Authenticator, Microsoft Authenticator, 1Password).
- WebAuthn / FIDO2 Passkey support for hardware security keys (YubiKey, Touch ID, Windows Hello).
- Mandatory MFA policy configurable per tenant (e.g. required for all administrators and mentors).

---

## 3. Authorization: Granular RBAC & ABAC

### 3.1 Role-Based Access Control (RBAC)
Spring Boot controllers protect routes using declarative method security:

```java
@PreAuthorize("hasAuthority('GRADE_ASSIGNMENT') and @tenantSecurity.isAssignedCohort(#batchId)")
@PostMapping("/cohorts/{batchId}/submissions/{id}/grade")
public ResponseEntity<GradeResponse> gradeSubmission(
    @PathVariable UUID batchId,
    @PathVariable UUID id,
    @RequestBody GradeRequest request) { ... }
```

### 3.2 Attribute-Based Access Control (ABAC)
Fine-grained authorization evaluates contextual dynamic attributes:
- **Tenancy Boundary:** `User.tenant_id == Resource.tenant_id`
- **Ownership Boundary:** `User.id == Submission.student_id OR User.id == Cohort.lead_instructor_id`
- **Temporal Boundary:** Live classroom access is permitted only between `scheduled_time - 15m` and `scheduled_time + duration + 30m`.
- **License Boundary:** Corporate learner access requires an active unexpired corporate seat allocated to their department.

---

## 4. Data Protection & Cryptographic Standards

### 4.1 Data in Transit
- **TLS 1.3 Mandatory:** Legacy SSLv3, TLS 1.0, and TLS 1.1 are explicitly disabled at Cloudflare and NGINX gateways.
- **HSTS Enforcement:** `Strict-Transport-Security: max-age=63072000; includeSubDomains; preload` header sent on all responses.
- **Perfect Forward Secrecy (PFS):** Modern cipher suites only (`ECDHE-ECDSA-AES256-GCM-SHA384`, `ECDHE-RSA-AES256-GCM-SHA384`).

### 4.2 Data at Rest
- **Database Storage:** AWS Aurora / EBS volumes encrypted with AES-256 utilizing AWS KMS customer-managed keys.
- **Object Storage (S3):** Server-Side Encryption with KMS keys (`SSE-KMS`) applied by default to all buckets.
- **Column-Level PII Encryption:** Sensitive fields (Tax IDs, bank account details for mentor payouts, emergency contact phone numbers) are encrypted at the JPA layer using AES-256-GCM before writing to PostgreSQL.

---

## 5. Regulatory Compliance & Privacy Governance

> [!NOTE]
> **Audit & Certification Status Disclaimer:** The policies and controls specified below represent target architectural blueprints designed for future certification audits (Phases 60–69). Nexarats Academy does not claim active SOC 2, ISO 27001, FERPA, or other third-party regulatory certification as of Phase 1. All institutional claims are subject to compliance verification.

### 5.1 General Data Protection Regulation (GDPR / UK GDPR)
- **Right of Access (Article 15):** Self-service "Export My Data" button in student settings generating a comprehensive machine-readable ZIP archive (JSON profile, learning history, forum posts, certificates).
- **Right to Erasure / Forgotten (Article 17):** Automated de-identification pipeline anonymizing user PII while preserving aggregate completion statistics for institutional accreditation.
- **Cookie Consent & Zero Tracking:** Strict compliance with ePrivacy Directive; no marketing tracking scripts load prior to explicit affirmative user consent.

### 5.2 Family Educational Rights and Privacy Act (FERPA)
- Student education records (grades, exam submissions, proctoring recordings, counselor notes) are classified as Restricted Confidential.
- Parents/guardians can only access student records through explicit student consent delegation or verified dependent linkage.
- Third-party hiring partners receive resume access only after affirmative student opt-in per placement drive.

### 5.3 SOC 2 Type II Security Controls
- **Audit Logging (Trust Services Criteria CC6.8):** All authentication events, privilege changes, data exports, and configuration updates are written to an immutable append-only PostgreSQL table and mirrored to AWS CloudWatch Logs.
- **Change Management:** All code releases require signed Git commits, automated CI/CD pipeline verification, and peer architect approval.
- **Vulnerability Management:** Weekly automated container image scans (Trivy), static application security testing (SAST), and Dependabot dependency alerts.
