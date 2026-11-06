# 41. PHASE 6 DATABASE, MEDIA & PROGRESS REPORT
**Project:** Nexarats Academy Enterprise Education Platform  
**Roadmap:** 70 Phases (Phase 0–69)  
**Implementation Phase:** Git Phase 6 (Implements Original Roadmap Phases 10 & 11)  
**Phase Title:** Student Learning Dashboard, Enrollment-Based Content Access, Secure Video Playback & Lesson Progress Tracking  
**Target Branch:** `master`  
**Execution Agent:** Gemini 3.8 Flash (High) — Antigravity (Audited & Reconciled)  
**Date:** October 13, 2026 (Audit Reconciled: November 2026)  
**Status:** RECOVERED & VERIFIED

---

## 1. Database Schema & Migration V10

Phase 6 introduces a forward-only database migration:
`V10__learner_progress_and_content_access.sql`

Applied cleanly on PostgreSQL 16.8 with zero regressions on V1–V9. The schema introduces 5 normalized tables with multi-tenant isolation, foreign key constraints, and optimistic concurrency locking.

> [!NOTE]
> Schema definitions below match the exact Flyway migration `V10__learner_progress_and_content_access.sql` and JPA entities. Earlier drafts that erroneously included synthetic counter columns (`completed_lessons_count`, `total_lessons_count`, `lesson_duration_seconds`) have been corrected. All counts and aggregate metrics are server-authoritative and dynamically queried from normalized records.

### 1. `learner_course_progress`
Maintains aggregate course completion state per learner and enrollment:
```sql
CREATE TABLE learner_course_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    learner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    curriculum_version_id UUID REFERENCES curriculum_versions(id) ON DELETE SET NULL,
    current_lesson_id UUID REFERENCES lessons(id) ON DELETE SET NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS',
    completion_percentage INTEGER NOT NULL DEFAULT 0 CHECK (completion_percentage >= 0 AND completion_percentage <= 100),
    completed_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_learner_course_enrollment UNIQUE (learner_user_id, course_id, enrollment_id)
);
```
- **Optimistic Locking:** `@Version private Long version;` prevents lost updates during concurrent progress writes across multiple devices/tabs.
- **Dynamic Lesson Counters:** Completed lessons are counted dynamically via `SELECT COUNT(*) FROM learner_lesson_progress WHERE learner_user_id = :userId AND course_id = :courseId AND is_completed = true` rather than denormalized counters that risk desynchronization.
- **Foreign Keys & Cascades:** Deleting a test learner safely cascades progress; tenant deletion is restricted.

### 2. `learner_lesson_progress`
Maintains granular per-lesson watch-time and playback position state:
```sql
CREATE TABLE learner_lesson_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    learner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    curriculum_version_id UUID REFERENCES curriculum_versions(id) ON DELETE SET NULL,
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED',
    watch_time_seconds INTEGER NOT NULL DEFAULT 0 CHECK (watch_time_seconds >= 0),
    last_playback_position_seconds INTEGER NOT NULL DEFAULT 0 CHECK (last_playback_position_seconds >= 0),
    completion_percentage INTEGER NOT NULL DEFAULT 0 CHECK (completion_percentage >= 0 AND completion_percentage <= 100),
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP WITH TIME ZONE,
    last_activity_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_learner_lesson_enrollment UNIQUE (learner_user_id, lesson_id, enrollment_id)
);
```

### 3. `learning_activity_records`
Audit trail of meaningful learning events with idempotency keys:
```sql
CREATE TABLE learning_activity_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    learner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    lesson_id UUID REFERENCES lessons(id) ON DELETE SET NULL,
    activity_type VARCHAR(64) NOT NULL,
    playback_position_seconds INTEGER DEFAULT 0,
    watch_time_delta_seconds INTEGER DEFAULT 0,
    metadata_json JSONB,
    idempotency_key VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```
- Annotated with `@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)` for PostgreSQL native JSONB persistence.
- Unique index on `idempotency_key` (WHERE `idempotency_key IS NOT NULL`) blocks double-counting from duplicated network retries.

### 4. `lesson_media_assets`
Metadata catalog for video and audio content assets:
```sql
CREATE TABLE lesson_media_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    asset_type VARCHAR(32) NOT NULL DEFAULT 'VIDEO_STREAM',
    storage_provider VARCHAR(32) NOT NULL DEFAULT 'INTERNAL_STREAMING',
    storage_object_key VARCHAR(512) NOT NULL,
    media_url VARCHAR(1024),
    duration_seconds INTEGER NOT NULL DEFAULT 0 CHECK (duration_seconds >= 0),
    content_version VARCHAR(32) NOT NULL DEFAULT '1.0.0',
    status VARCHAR(32) NOT NULL DEFAULT 'READY',
    access_classification VARCHAR(32) NOT NULL DEFAULT 'ENROLLED_ONLY',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_lesson_media_asset UNIQUE (lesson_id, asset_type)
);
```

### 5. `playback_sessions`
Cryptographically secured short-lived media stream tokens:
```sql
CREATE TABLE playback_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_token VARCHAR(128) NOT NULL UNIQUE,
    learner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    asset_id UUID REFERENCES lesson_media_assets(id) ON DELETE SET NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_heartbeat_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 2. Media Delivery & Video Streaming Pipeline

1. **Session Authorization & Storage Resolution:**
   - Client requests `POST /api/v1/learning/lessons/{lessonId}/playback-session`.
   - Backend calls `LearnerEntitlementService` to verify active enrollment in the course or owning career-track program within the caller's tenant.
   - If verified, creates a `PlaybackSession` with token `vtok_<uuid>` expiring in 2 hours (`Duration.ofHours(2)`).
   - Resolves `LessonMediaAsset` for the requested lesson and validates that asset status is `READY`.
   - Returns stream URL `/api/v1/learning/media/stream?token=vtok_...` and duration.

2. **Storage Abstraction (`MediaStorageService`):**
   - Media resolution uses `MediaStorageService` interface implemented by `DefaultMediaStorageService`.
   - Resolves media by asset `storageProvider` and `storageObjectKey` (supporting classpath resources, filesystem paths, and ready for S3/MinIO in Phase 14).
   - Fails securely with HTTP 404 if the media resource is missing on storage, and HTTP 409 if transcoding. Never falls back to dummy sample video in production environments.

3. **RFC 7233 Byte-Range Streaming:**
   - HTML5 video element requests `GET /api/v1/learning/media/stream?token=vtok_...` with optional `Range: bytes=start-end` header.
   - `VideoPlaybackService` parses byte range, validates active session token, enrollment status (`ACTIVE`/`ENROLLED`), and expiry.
   - Returns HTTP 206 Partial Content with headers:
     - `Content-Range: bytes start-end/totalLength`
     - `Content-Length: chunkLength`
     - `Content-Type: video/mp4`
     - `Accept-Ranges: bytes`
   - Enables native browser timeline scrubbing, seeking, and adaptive buffer preloading without loading entire video files into memory.

4. **Storage Security:**
   - Raw disk/storage keys and bucket paths are never exposed to the client.
   - Streaming is permitted only via server-validated session tokens. Expired, forged, or cross-tenant tokens return HTTP 403 Forbidden.

---

## 3. Server-Authoritative Progress Calculation

1. **Checkpoint Ingestion:**
   - Receives `playbackPositionSeconds`, `watchTimeDeltaSeconds`, and `idempotencyKey`.
   - Plausibility boundaries enforced:
     - `playbackPositionSeconds >= 0`
     - `0 <= watchTimeDeltaSeconds <= 300` (rejects unrealistic delta spikes)
   - Idempotency check: If an activity record exists with the identical `idempotencyKey`, delta watch-time is not incremented, and current status is returned idempotently.

2. **Client-Side Watch-Time Tracking (`VideoPlayer.tsx`):**
   - Playback position (`currentTime`) is strictly decoupled from elapsed watch-time.
   - Wall-clock playback progression is tracked only while playing and unpaused.
   - Forward or backward seeking does not accumulate watch time credit, preventing false completion and preventing HTTP 400 Bad Request on seeks > 300s.

3. **Lesson Completion Threshold:**
   - Lesson is marked completed when:
     - Explicit completion triggered (`POST /complete`), OR
     - Playback position reaches >= 90% of duration, OR
     - Cumulative watch time reaches >= 80% of lesson duration.
   - Once marked completed, a lesson remains completed monotonically.

4. **Course & Program Progress Aggregation:**
   - Course completion percentage is strictly calculated server-side:
     $$\text{Course \%} = \left\lfloor \frac{\text{Completed Lessons}}{\text{Total Lessons}} \times 100 \right\rfloor$$
   - Program progress is calculated across all required courses belonging to the program via `program_courses`.
   - Never trusts client-submitted overall progress percentages.
