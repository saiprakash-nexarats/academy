# 41. PHASE 6 DATABASE, MEDIA & PROGRESS REPORT
**Project:** Nexarats Academy Enterprise Education Platform  
**Roadmap:** 70 Phases (Phase 0–69)  
**Phase:** Phase 6 — Student Learning Dashboard, Enrollment-Based Content Access, Secure Video Playback & Lesson Progress Tracking  
**Target Branch:** `master`  
**Execution Agent:** Gemini 3.8 Flash (High) — Antigravity  
**Date:** October 13, 2026  
**Status:** COMPLETE & PASSING

---

## 1. Database Schema & Migration V10

Phase 6 introduces a forward-only database migration:
`V10__learner_progress_and_content_access.sql`

Applied cleanly on PostgreSQL 16.8 with zero regressions on V1–V9. The schema introduces 5 normalized tables with multi-tenant isolation, foreign key constraints, and optimistic concurrency locking.

### 1. `learner_course_progress`
Maintains aggregate course completion state per learner and enrollment:
```sql
CREATE TABLE learner_course_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE RESTRICT,
    learner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    curriculum_version_id UUID REFERENCES curriculum_versions(id) ON DELETE RESTRICT,
    current_lesson_id UUID REFERENCES lessons(id) ON DELETE SET NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS',
    completion_percentage INT NOT NULL DEFAULT 0 CHECK (completion_percentage >= 0 AND completion_percentage <= 100),
    completed_lessons_count INT NOT NULL DEFAULT 0 CHECK (completed_lessons_count >= 0),
    total_lessons_count INT NOT NULL DEFAULT 0 CHECK (total_lessons_count >= 0),
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_learner_course_progress UNIQUE (learner_user_id, course_id, enrollment_id)
);
```
- **Optimistic Locking:** `@Version private Long version;` prevents lost updates during concurrent progress writes across multiple devices/tabs.
- **Foreign Keys & Cascades:** Deleting a test learner safely cascades progress; tenant deletion is restricted.

### 2. `learner_lesson_progress`
Maintains granular per-lesson watch-time and playback position state:
```sql
CREATE TABLE learner_lesson_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    learner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    curriculum_version_id UUID REFERENCES curriculum_versions(id) ON DELETE RESTRICT,
    status VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED',
    watch_time_seconds INT NOT NULL DEFAULT 0 CHECK (watch_time_seconds >= 0),
    last_playback_position_seconds INT NOT NULL DEFAULT 0 CHECK (last_playback_position_seconds >= 0),
    lesson_duration_seconds INT NOT NULL DEFAULT 0 CHECK (lesson_duration_seconds >= 0),
    completion_percentage INT NOT NULL DEFAULT 0 CHECK (completion_percentage >= 0 AND completion_percentage <= 100),
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    content_version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    CONSTRAINT uq_learner_lesson_progress UNIQUE (learner_user_id, lesson_id, enrollment_id)
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
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    activity_type VARCHAR(64) NOT NULL,
    playback_position_seconds INT DEFAULT 0,
    watch_time_delta_seconds INT DEFAULT 0,
    metadata_json JSONB,
    idempotency_key VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```
- Annotated with `@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)` for PostgreSQL native JSONB persistence.
- Unique index on `idempotency_key` (WHERE `idempotency_key IS NOT NULL`) blocks double-counting from duplicated network retries.

### 4. `lesson_media_assets`
Metadata catalog for audio/video assets:
```sql
CREATE TABLE lesson_media_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    asset_type VARCHAR(32) NOT NULL DEFAULT 'VIDEO_MP4',
    storage_provider VARCHAR(32) NOT NULL DEFAULT 'LOCAL_STORAGE',
    storage_object_key VARCHAR(512) NOT NULL,
    mime_type VARCHAR(64) NOT NULL DEFAULT 'video/mp4',
    duration_seconds INT NOT NULL DEFAULT 0 CHECK (duration_seconds >= 0),
    content_version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    processing_status VARCHAR(32) NOT NULL DEFAULT 'READY',
    access_classification VARCHAR(32) NOT NULL DEFAULT 'ENROLLED_ONLY',
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
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
    expires_at TIMESTAMPTZ NOT NULL,
    last_heartbeat_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

## 2. Media Delivery & Video Streaming Pipeline

1. **Session Authorization:**
   - Client requests `POST /api/v1/learning/lessons/{lessonId}/playback-session`.
   - Backend calls `LearnerEntitlementService` to verify active enrollment in the course or owning career-track program.
   - If verified, creates a `PlaybackSession` with token `vtok_<uuid>` expiring in 60 minutes.
   - Returns stream URL `/api/v1/learning/media/stream?token=vtok_...` and duration.

2. **RFC 7233 Byte-Range Streaming:**
   - HTML5 video element requests `GET /api/v1/learning/media/stream?token=vtok_...` with optional `Range: bytes=start-end` header.
   - `VideoPlaybackService` parses byte range, validates active session token and expiry.
   - Returns HTTP 206 Partial Content with headers:
     - `Content-Range: bytes start-end/totalLength`
     - `Content-Length: chunkLength`
     - `Content-Type: video/mp4`
     - `Accept-Ranges: bytes`
   - Enables native browser timeline scrubbing, seeking, and adaptive buffer preloading without loading entire video files into memory.

3. **Storage Security:**
   - Raw disk/storage keys and bucket paths are never exposed to the client.
   - Streaming is permitted only via server-validated session tokens. Expired or forged tokens return HTTP 403 Forbidden.

---

## 3. Server-Authoritative Progress Calculation

1. **Checkpoint Ingestion:**
   - Receives `playbackPositionSeconds`, `watchTimeDeltaSeconds`, and `idempotencyKey`.
   - Plausibility boundaries enforced:
     - `playbackPositionSeconds >= 0`
     - `0 <= watchTimeDeltaSeconds <= 300` (rejects unrealistic delta spikes)
   - Idempotency check: If an activity record exists with the identical `idempotencyKey`, delta watch-time is not incremented, and current status is returned idempotently.

2. **Lesson Completion Threshold:**
   - Lesson is marked completed when:
     - Explicit completion triggered (`POST /complete`), OR
     - Playback position reaches >= 90% of duration, OR
     - Cumulative watch time reaches >= 80% of lesson duration.
   - Once marked completed, a lesson remains completed monotonically.

3. **Course & Program Progress Aggregation:**
   - Course completion percentage is strictly calculated server-side:
     $$\text{Course \%} = \left\lfloor \frac{\text{Completed Lessons}}{\text{Total Lessons}} \times 100 \right\rfloor$$
   - Program progress is calculated across all required courses belonging to the program via `program_courses`.
   - Never trusts client-submitted overall progress percentages.
