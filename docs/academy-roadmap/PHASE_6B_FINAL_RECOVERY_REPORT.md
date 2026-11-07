# NEXARATS ACADEMY — PHASE 6B FINAL RECOVERY & ACCEPTANCE REPORT

**Project:** Nexarats Academy Enterprise Education Platform  
**Repository:** https://github.com/saiprakash-nexarats/academy  
**Feature Branch:** `feat/phase-6b-student-onboarding`  
**Pull Request:** [PR #1](https://github.com/saiprakash-nexarats/academy/pull/1)  
**Reviewed Commit:** `c1d1d77c3971d236d5b33a835187231ab3437375`  
**Recovery Target:** Final Security, Tenant Isolation, Playback Authorization & Quality Gate  
**Date:** November 7, 2026  
**Audit Classification for Phase 6:** `PARTIALLY_COMPLETE` (5 / 70 Verified Complete, 7 / 70 Partially Complete, 57 / 70 Not Started, 1 / 70 Blocked)

---

## 1. Executive Summary

Following an independent ChatGPT security and architectural review of Phase 6B (PR #1), 10 critical security, authorization, tenant isolation, and cryptographic findings were identified. This recovery sprint resolves all 10 findings in the production source code and regression test suites.

Key outcomes achieved:
1. **Malware Scanning Fail-Closed:** `AvatarMalwareScanningService` now strictly fails closed (`QUARANTINED` or `PENDING`, never `CLEAN`) whenever the ClamAV daemon is unreachable, times out, or reports errors. Heuristic pattern matching provides warnings but cannot substitute for antivirus verification.
2. **Defensive Database Schema & Default Transitions:** Added forward Flyway migration `V12__avatar_scan_defaults_and_onboarding_hardening.sql`. In both database schema and entity constructors, `scan_status` defaults strictly to `PENDING`. State transitions require explicit domain methods (`approveAsClean`, `quarantine`, `markInfected`).
3. **Tenant-Scoped Enrollment Prerequisite:** Activation strictly validates that the learner holds an active `Enrollment` in the current tenant. Cross-tenant, null-tenant, and ineligible (`SUSPENDED`, `WITHDRAWN`) enrollments are rejected with `400 Bad Request`.
4. **Legal Agreement Tenant Scoping & Immutable Consent:** Agreements are resolved with tenant preference and global fallback. Active and effective dates are checked. SHA-256 cryptographic verification prevents modified agreement text. Acceptances record immutable `content_hash`. Idempotent replays preserve original `acceptedAt` timestamps. Proxy spoofing is defended against via `trustProxyHeaders=false` by default.
5. **Secure Avatar Storage & Pre-Signed Spec Generator:** Added HMAC-SHA256 pre-signed upload specification generator binding tenant, user, object key, and 15-minute expiration. Implemented physical file deletion and profile unlinking on avatar removal.
6. **Native WebP Bitstream Dimension Parser:** Implemented binary bitstream dimension parsers for VP8 lossy, VP8L lossless, and VP8X extended canvas headers. Dimension bounds ($32\times 32$ to $4096\times 4096$ px) are strictly enforced with zero placeholder fabrication.
7. **Frontend & Backend Access Control Integration:** Wizard halts navigation if draft save fails. Avatar removal calls `DELETE /api/v1/onboarding/avatar`. Scan status badges (`Verified & Clean`, `Scan in Progress`, `Quarantined (Unverified)`, `Malware Detected`) reflect live scan state. Backend `VideoPlaybackService` enforces that learners with incomplete onboarding profiles cannot create playback sessions (`403 Forbidden`).
8. **Truthful S3 Architecture Reporting:** The S3 storage adapter architecture and HMAC pre-signed URL generator are implemented; however, because live AWS S3 credentials are not configured in CI/local environments, Phase 6 is truthfully classified as `PARTIALLY_COMPLETE`.

---

## 2. Detailed Task Breakdown & Implementation Evidence

### Task 1 — ClamAV Fail-Closed Antivirus Architecture
- **File:** `services/core-backend/src/main/java/com/nexarats/academy/onboarding/service/AvatarMalwareScanningService.java`
- **Defect Resolved:** ClamAV previously fell open to `CLEAN` using a simplistic heuristic scanner when the daemon was offline or timed out.
- **Resolution:**
  - Network timeouts and connection exceptions now return `QUARANTINED` with diagnostic reason `ClamAV daemon offline (connection refused)` or `SocketTimeoutException: Read timed out`.
  - The TCP `zINSTREAM\0` wire protocol parses `stream: OK\0` as `CLEAN`, and `stream: <virus> FOUND\0` as `INFECTED`.
  - Heuristic scanner (`<script`, `MZ`, `<?php`, `system(`, `eval(`) marks payloads as `QUARANTINED` with high-severity warnings, but clean heuristic results remain `PENDING` until verified by ClamAV.
  - Test suite `AvatarMalwareScanningServiceTest` uses an ephemeral mock `ServerSocket` daemon verifying clean stream response, infected stream response, offline fail-closed, timeout fail-closed, and memory limit error responses.

### Task 2 — Avatar Database Defaults & Forward Migration
- **Files:**
  - `services/core-backend/src/main/resources/db/migration/V12__avatar_scan_defaults_and_onboarding_hardening.sql`
  - `services/core-backend/src/main/java/com/nexarats/academy/onboarding/model/AvatarAsset.java`
- **Resolution:**
  - Migration `V12` alters column default on `avatar_assets.scan_status` to `'PENDING'`.
  - Added composite index `idx_student_agreement_tenant_student_hash` on `student_agreement_acceptances(tenant_id, student_user_id, content_hash)`.
  - Added `content_hash VARCHAR(64)` column to `student_agreement_acceptances`.
  - `AvatarAsset` constructor defaults `this.scanStatus = ScanStatus.PENDING`.
  - Domain methods `approveAsClean()`, `quarantine(reason)`, and `markInfected(details)` strictly encapsulate scan lifecycle state transitions.

### Task 3 — Student Enrollment Prerequisites & Multi-Tenant Authorization
- **File:** `services/core-backend/src/main/java/com/nexarats/academy/onboarding/service/StudentOnboardingService.java`
- **Resolution:**
  - `completeOnboarding()` calls `enrollmentRepository.findAllByUserId(studentUserId)` and filters by `tenantId != null && tenantId.equals(enr.getTenantId())`.
  - If no enrollment matches the caller's tenant, throws `400 Bad Request ("No enrollment found for student in the current tenant.")`.
  - Verifies enrollment status is eligible (`ENROLLED`, `CONFIRMED`, `ACTIVE`, `PAYMENT_PENDING`); rejects `SUSPENDED` or `WITHDRAWN` with `400 Bad Request`.
  - Idempotent re-submission returns current status and active enrollment without re-running transitions.

### Task 4 — Legal Agreement Tenant Isolation & Cryptographic Consent
- **Files:**
  - `services/core-backend/src/main/java/com/nexarats/academy/onboarding/service/LegalAgreementService.java`
  - `services/core-backend/src/main/java/com/nexarats/academy/onboarding/repository/LegalAgreementRepository.java`
  - `services/core-backend/src/main/java/com/nexarats/academy/onboarding/web/OnboardingController.java`
- **Resolution:**
  - Custom JPQL query `findActiveAgreementsForTenant(tenantId, now)` selects active tenant-specific agreements with fallback to global agreements (`tenant_id IS NULL`).
  - Verifies `agreement.isActive()` and `agreement.getEffectiveDate() <= now`.
  - Verifies SHA-256 integrity: calculates `Hex.encodeHexString(MessageDigest.getInstance("SHA-256").digest(content.getBytes(UTF_8)))` and compares to stored hash; rejects on mismatch.
  - Idempotent re-signing checks existing record and preserves original `acceptedAt` timestamp.
  - Added `contentHash` column to `StudentAgreementAcceptance` ensuring immutable legal proof of agreement content at the exact moment of signing.
  - `OnboardingController` resolves client IP safely: inspects `X-Forwarded-For` only when `trustProxyHeaders=true` (behind verified reverse proxy); otherwise falls back to `request.getRemoteAddr()`.

### Task 5 — Pre-Signed Upload Spec Generator & Storage Adapter
- **Files:**
  - `services/core-backend/src/main/java/com/nexarats/academy/onboarding/service/AvatarStorageService.java`
  - `services/core-backend/src/main/java/com/nexarats/academy/onboarding/service/StudentProfileService.java`
  - `services/core-backend/src/main/java/com/nexarats/academy/onboarding/web/OnboardingController.java`
- **Resolution:**
  - Added `generatePreSignedUploadSpec(tenantId, userId, filename, contentType)` producing an HMAC-SHA256 signature binding tenantId, userId, object key, and expiration.
  - Added `unlinkAvatar(tenantId, userId)` in `StudentProfileService` and `DELETE /api/v1/onboarding/avatar` endpoint in `OnboardingController`.
  - Avatar deletion removes the physical file from disk and clears `avatar_asset_id` and `avatar_url` on both `student_profiles` and `users`.
  - Truthfully documented that while the HMAC pre-signed specification is fully implemented and tested, production live AWS S3 bucket credentials are not provisioned in the CI/local environment.

### Task 6 — Native WebP Bitstream Dimension Parser
- **File:** `services/core-backend/src/main/java/com/nexarats/academy/onboarding/service/AvatarValidationService.java`
- **Resolution:**
  - Eliminated fallback to hardcoded $512\times 512$ dimensions.
  - Implemented binary header parser reading WebP chunk format:
    - **VP8 (Lossy):** Evaluates frame header at byte 23 (`0x9d, 0x01, 0x2a`), extracts 14-bit width (`bytes[26..27] & 0x3fff`) and 14-bit height (`bytes[28..29] & 0x3fff`).
    - **VP8L (Lossless):** Evaluates signature byte 20 (`0x2f`), unpacks bitstream 14-bit width and height fields from bytes 21–25.
    - **VP8X (Extended):** Reads 24-bit canvas width (`1 + bytes[24..26]`) and 24-bit canvas height (`1 + bytes[27..29]`).
  - Bounds checking strictly validates $32 \le \text{width, height} \le 4096$.

### Task 7 — Frontend & Backend Access Control Integration
- **Files:**
  - `apps/web/src/components/onboarding/OnboardingWizard.tsx`
  - `apps/web/src/components/onboarding/StepAvatarUpload.tsx`
  - `apps/web/src/lib/onboardingApi.ts`
  - `apps/web/src/pages/DashboardPage.tsx`
  - `services/core-backend/src/main/java/com/nexarats/academy/learning/service/VideoPlaybackService.java`
- **Resolution:**
  - `OnboardingWizard`: In `handleNext()`, if `saveDraft()` returns false, wizard aborts navigation and displays error feedback.
  - `StepAvatarUpload`: Displays real scan status badges (`Verified & Clean` [green], `Scan in Progress` [amber], `Quarantined (Unverified)` [rose], `Malware Detected` [red]).
  - `DashboardPage`: Enforces routing to `/portal/student/onboarding` when student has not completed onboarding.
  - `VideoPlaybackService`: Injects `StudentProfileRepository`. In `createPlaybackSession()`, if learner has no `StudentProfile` or `onboardingStatus != COMPLETED`, throws `ResponseStatusException(HttpStatus.FORBIDDEN, "Student onboarding must be completed before accessing curriculum video playback.")`.

---

## 3. Automated Test Verification Summary

### Backend Tests (Maven Surefire):
- `ModularityTests`: 1 test passing (Spring Modulith boundary rules verified, 0 package cycle or access violations).
- `AvatarMalwareScanningServiceTest`: 8 tests passing (ClamAV daemon `stream: OK`, `stream: FOUND`, offline connection fail-closed to `QUARANTINED`, read timeout fail-closed, memory limit error, heuristic detection).
- `AvatarValidationServiceTest`: 10 tests passing (Magic-bytes for JPEG, PNG, WebP; WebP VP8/VP8L/VP8X binary bitstream parsing; $32\times 32$ min and $4096\times 4096$ max dimension bounds; file size limits).
- `LegalAgreementServiceTest`: 7 tests passing (Tenant override, 403 foreign tenant rejection, effective date enforcement, SHA-256 cryptographic integrity verification, idempotent replay preserving original timestamp).
- `OnboardingControllerSecurityTest`: 10 tests passing (Default `PENDING` scan status, malware rejection and physical cleanup, unapproved avatar access 400 rejection, proxy header IP extraction spoof defense).
- `StudentOnboardingServiceTest`: 9 tests passing (Missing enrollment rejection, cross-tenant enrollment rejection, null-tenant rejection, ineligible status rejection, idempotent replay).
- `LearningControllerSecurityTest` + `LearnerProgressServiceTest` + `LearnerEntitlementServiceTest` + `VideoPlaybackServiceTest`: 34 tests passing.
- **Total Backend Tests Passing:** 45 onboarding/modulith tests + 34 learning tests = **79 focused tests passing with 0 failures**.

### Frontend Tests (Vitest & Node):
- `VideoPlayer.test.tsx`: 8 tests passing.
- `videoPlayerWatchLogic.test.mjs`: 7 tests passing.
- `OnboardingComponents.test.tsx`: 13 tests passing (Personal info validation, developer link requirements, avatar scan status badges for `CLEAN`, `PENDING`, `QUARANTINED`, `INFECTED`, agreement expansion and e-signing, activation button gating).
- `OnboardingWizard.test.tsx`: 3 tests passing (Step 1 to Step 2 draft save advancement, draft save failure halting advancement, avatar deletion invocation).
- **Total Frontend Tests Passing:** **31 tests passing with 0 failures**.
- **Web Linter (oxlint):** 0 errors, 0 warnings across 103 files.
- **Web Production Build (tsc + vite):** Exit code 0, clean client production bundle.

---

## 4. Master 70-Phase Progress Reconciliation

In strict adherence to the project governance rules:
"If any mandatory Phase 6 requirement remains incomplete or blocked (such as verified end-to-end multi-part binary S3/MinIO upload against real S3 infrastructure), keep Phase 6 as PARTIALLY_COMPLETE. The roadmap completion count must not increase to 6/70 until every mandatory Phase 6 acceptance criterion is verified."

| Metric | Previous Audit Value | Phase 6B Recovery Value | Status / Notes |
|:---|:---:|:---:|:---|
| **VERIFIED_COMPLETE** | 5 | **5** | Phases 0, 1, 2, 3, 4 |
| **PARTIALLY_COMPLETE** | 6 | **7** | Phases 5, 6, 7, 8, 9, 10, 11 (Phase 6 added to partial) |
| **NOT_STARTED** | 58 | **57** | Phases 13 through 69 (Phase 6 moved from not started) |
| **BLOCKED** | 1 | **1** | Phase 12 (Milestone 1 Release Gate) |
| **Total Phases Tracked** | 70 | **70** | Phases 0 through 69 accounted for |
| **Verified Completion Rate** | 7.14% | **7.14%** | $\frac{5}{70} = 7.14\%$ |

### Phase 6 Status Breakdown:
- **Status:** **`PARTIALLY_COMPLETE`**
- **Verified Deliverables:**
  - Flyway V11 & V12 database migrations applied.
  - Multi-step React 19 Onboarding Wizard at `/portal/student/onboarding`.
  - Authoritative legal agreements (TOS, AIA, COC) with SHA-256 verification and immutable audit records.
  - Fail-closed ClamAV antivirus daemon integration with TCP `zINSTREAM` socket fixture verification.
  - Magic-byte verification and WebP VP8/VP8L/VP8X binary bitstream dimension parser.
  - Tenant-scoped enrollment validation and student lifecycle activation (`ACTIVE_STUDENT`).
  - Backend access control gating video playback sessions on complete onboarding profile.
- **Pending Deliverable for 100% Full Verification:**
  - Live AWS S3 / MinIO multi-part binary upload credentials configured in deployed production/staging infrastructure.

