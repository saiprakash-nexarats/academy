# NEXARATS ACADEMY — ARCHITECTURAL & IMPLEMENTATION DECISIONS (ADR)

**Document ID:** `IMPLEMENTATION_DECISIONS`  
**Classification:** Architecture Decision Records (ADRs)  
**Project:** Nexarats Academy  
**Status:** ACTIVE  

---

## ADR-001: Separation of Media Storage Abstraction from Streaming Controller
- **Context:** `VideoPlaybackService` initially attempted to read files directly from the classpath using a hardcoded `sample_lesson.mp4` path. This blocked multi-asset delivery and prevented future integration with external object storage (S3/MinIO in Phase 14).
- **Decision:** Introduce `MediaStorageService` as a clean abstraction representing asset retrieval by `storageProvider` and `storageObjectKey`. The initial implementation `DefaultMediaStorageService` supports classpath and filesystem resolution, and easily accepts cloud S3 pre-signed or direct stream adapters without modifying `VideoPlaybackService`.
- **Status:** APPROVED & IMPLEMENTED in Phase 6 Recovery.

---

## ADR-002: Client-Side Separation of Playback Position from Watch Time
- **Context:** `VideoPlayer.tsx` accumulated watch time by calculating `delta = currentTime - lastCheckpointTime`. When a student scrubbed or sought forward across the timeline, this produced massive artificial deltas, triggering HTTP 400 Bad Request (`watchTimeDeltaSeconds > 300`) and crediting unearned watch time.
- **Decision:** Playback position (`currentTime`) represents the video playhead. Watch-time credit is strictly calculated from elapsed wall-clock time during active, unpaused playback (`!paused && !seeking`). Seek events reset the delta anchor without adding watch time.
- **Status:** APPROVED & IMPLEMENTED in Phase 6 Recovery.

---

## ADR-003: Multi-Tenant Scoping in Service Queries
- **Context:** Prior service methods relied solely on `learnerUserId` without checking the authenticated user's `tenantId`. In a multi-tenant enterprise architecture, user IDs across tenants or mismatched tenant tokens could lead to horizontal data leakage.
- **Decision:** All entitlement, dashboard, and progress services accept `tenantId` and query enrollment and course progress with explicit tenant constraints. `LearningController` extracts `principal.getTenantId()` and enforces it across all read/write paths.
- **Status:** APPROVED & IMPLEMENTED in Phase 6 Recovery.

---

## ADR-004: Test Strategy — Isolated Unit Suites vs. Container Integration Tests
- **Context:** Spring Boot integration tests annotated with `@ActiveProfiles("local")` require live PostgreSQL (port 5432) and Redis (port 6379). Developers or build environments lacking a running Docker daemon faced immediate build failures.
- **Decision:** Maintain a two-tier test strategy:
  1. **Tier 1 (Fast, Containerless):** Unit tests using Mockito and Spring Modulith ArchUnit tests (`VideoPlaybackServiceTest`, `LearnerProgressServiceTest`, `LearnerEntitlementServiceTest`, `LearningControllerSecurityTest`, `ModularityTests`) running in standard JVMs in < 25s without external dependencies.
  2. **Tier 2 (Full Integration):** Containerized tests (`@ActiveProfiles("local")`) executed in CI where Docker containers are spun up automatically.
- **Status:** APPROVED & IMPLEMENTED in Phase 6 Recovery.

---

## ADR-005: Dynamic Lesson Counters vs. Denormalized Aggregates
- **Context:** Report 41 documented synthetic columns `completed_lessons_count` and `total_lessons_count` on `learner_course_progress`, which do not exist in Flyway `V10__learner_progress_and_content_access.sql`.
- **Decision:** Retain normalized, authoritative counting via `learner_lesson_progress` queries. Denormalized counter columns are prone to write drift and race conditions during concurrent multi-tab sessions.
- **Status:** APPROVED & DOCUMENTED in Phase 6 Recovery.

---

## ADR-006: Student Onboarding Lifecycle, Avatar Security & ClamAV Heuristic Resilience
- **Context:** Original Roadmap Phase 6 required ClamAV anti-malware scanning for profile avatars. In local environments or lightweight CI runners without a ClamAV daemon container, connecting to port 3310 causes connection errors.
- **Decision:** Implement a dual-mode scanning service: attempt live ClamAV daemon connection via TCP `zINSTREAM`. If unreachable, fallback to deep binary heuristic pattern inspection (PHP tags, `<script>`, shell shebangs, MZ/ELF headers) and transparently record the heuristic scan status in the audit log without pretending a live ClamAV daemon ran.
- **Status:** APPROVED & IMPLEMENTED in Phase 6B.

---

## ADR-008: Two-Stage Quarantine-to-Approved S3 Key Architecture & Overwrite Defense
- **Context:** Pre-signed PUT URLs allow direct client uploads to object storage. If the issued pre-signed URL points directly to the final avatar key and remains valid for 15 minutes, a client could overwrite approved bytes with unscanned malware after the server marked the asset clean.
- **Decision:** Pre-signed PUT URLs target temporary, isolated quarantine keys only (`quarantine/avatars/{tenantId}/{userId}/{uuid}.{ext}`). Server issues and records single-use `avatar_upload_tickets` in PostgreSQL (Flyway V14). Upon MIME validation and clean ClamAV scan, verified bytes are atomically promoted to a brand-new, unguessable approved key (`avatars/{tenantId}/{userId}/{uuid}.{ext}`), and the quarantined object is deleted from storage. Even if the pre-signed URL is re-executed, it cannot alter the approved avatar, and re-confirmation fails with HTTP 400 Bad Request.
- **Status:** APPROVED & IMPLEMENTED in Phase 6B Final Security Hardening.

---

## ADR-009: Fail-Closed S3 Presigning, Bounded Stream Inspection & Playback Stream Profile Defense
- **Context:** Silently falling back to fabricated HMAC URLs in S3 mode causes nonfunctional upload loops. Unbounded S3 byte downloads risk heap exhaustion (OOM). Additionally, if a student profile is deleted after session creation, streaming media chunks should not proceed.
- **Decision:** In `S3_COMPATIBLE` mode, `AvatarStorageService` explicitly fails with `IllegalStateException` if `s3Presigner` is missing. Bounded streaming enforces S3 `headObject` verification and max 5MB streaming limit. In `VideoPlaybackService.streamMedia()`, missing or deleted student profiles immediately return HTTP 403 Forbidden, preserving verified staff/admin exemptions.
- **Status:** APPROVED & IMPLEMENTED in Phase 6B Final Security Hardening.


---

## ADR-007: Cryptographic Legal Agreement Integrity & Clickwrap Audit Trail
- **Context:** Legal terms (Terms of Service, Academic Integrity, Code of Conduct) must be legally enforceable and immune to silent post-acceptance modifications.
- **Decision:** Persist SHA-256 hashes of agreement content in `legal_agreements.content_hash`. Re-compute and verify the hash before recording e-signatures in `student_agreement_acceptances` with client IP, user agent, and timestamp.
- **Status:** APPROVED & IMPLEMENTED in Phase 6B.

---

## ADR-008: Two-Stage Quarantine-to-Approved S3 Key Architecture & Overwrite Defense
- **Context:** Pre-signed PUT URLs allow direct client uploads to object storage. If the issued pre-signed URL points directly to the final avatar key and remains valid for 15 minutes, a client could overwrite approved bytes with unscanned malware after the server marked the asset clean.
- **Decision:** Pre-signed PUT URLs target temporary, isolated quarantine keys only (`quarantine/avatars/{tenantId}/{userId}/{uuid}.{ext}`). Server issues and records single-use `avatar_upload_tickets` in PostgreSQL (Flyway V14). Upon MIME validation and clean ClamAV scan, verified bytes are atomically promoted to a brand-new, unguessable approved key (`avatars/{tenantId}/{userId}/{uuid}.{ext}`), and the quarantined object is deleted from storage. Even if the pre-signed URL is re-executed, it cannot alter the approved avatar, and re-confirmation fails with HTTP 400 Bad Request.
- **Status:** APPROVED & IMPLEMENTED in Phase 6B Final Security Hardening.

---

## ADR-009: Fail-Closed S3 Presigning, Bounded Stream Inspection & Playback Stream Profile Defense
- **Context:** Silently falling back to fabricated HMAC URLs in S3 mode causes nonfunctional upload loops. Unbounded S3 byte downloads risk heap exhaustion (OOM). Additionally, if a student profile is deleted after session creation, streaming media chunks should not proceed.
- **Decision:** In `S3_COMPATIBLE` mode, `AvatarStorageService` explicitly fails with `IllegalStateException` if `s3Presigner` is missing. Bounded streaming enforces S3 `headObject` verification and max 5MB streaming limit. In `VideoPlaybackService.streamMedia()`, missing or deleted student profiles immediately return HTTP 403 Forbidden, preserving verified staff/admin exemptions.
- **Status:** APPROVED & IMPLEMENTED in Phase 6B Final Security Hardening.


