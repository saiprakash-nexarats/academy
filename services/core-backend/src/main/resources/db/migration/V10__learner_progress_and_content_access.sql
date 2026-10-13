-- ============================================================================
-- NEXARATS ACADEMY — FLYWAY MIGRATION V10
-- Learner Progress, Enrolled Content Access, Media Assets & Playback Sessions
-- Phase 6: Student Learning Dashboard, Video LMS Delivery & Progress Engine
-- ============================================================================

-- 1. Learner Course Progress Table
-- Tracks overarching student progress for an enrolled course
CREATE TABLE IF NOT EXISTS learner_course_progress (
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

CREATE INDEX IF NOT EXISTS idx_lcp_learner ON learner_course_progress(learner_user_id);
CREATE INDEX IF NOT EXISTS idx_lcp_enrollment ON learner_course_progress(enrollment_id);
CREATE INDEX IF NOT EXISTS idx_lcp_course ON learner_course_progress(course_id);
CREATE INDEX IF NOT EXISTS idx_lcp_tenant ON learner_course_progress(tenant_id);

-- 2. Learner Lesson Progress Table
-- Granular per-lesson tracking (watch time, playback resume point, completion status)
CREATE TABLE IF NOT EXISTS learner_lesson_progress (
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

CREATE INDEX IF NOT EXISTS idx_llp_learner_course ON learner_lesson_progress(learner_user_id, course_id);
CREATE INDEX IF NOT EXISTS idx_llp_lesson ON learner_lesson_progress(lesson_id);
CREATE INDEX IF NOT EXISTS idx_llp_enrollment ON learner_lesson_progress(enrollment_id);

-- 3. Learning Activity Records Table
-- Deduplicated, auditable event stream for learning milestones and checkpoints
CREATE TABLE IF NOT EXISTS learning_activity_records (
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

CREATE INDEX IF NOT EXISTS idx_lar_learner_course ON learning_activity_records(learner_user_id, course_id);
CREATE INDEX IF NOT EXISTS idx_lar_idempotency ON learning_activity_records(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_lar_created_at ON learning_activity_records(created_at);

-- 4. Lesson Content Media Assets Table
-- Authoritative catalog of media streams and assets associated with lessons
CREATE TABLE IF NOT EXISTS lesson_media_assets (
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

CREATE INDEX IF NOT EXISTS idx_lma_lesson ON lesson_media_assets(lesson_id);

-- 5. Playback Sessions Table
-- Short-lived, secure playback authorization tokens for media delivery
CREATE TABLE IF NOT EXISTS playback_sessions (
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

CREATE INDEX IF NOT EXISTS idx_ps_token ON playback_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_ps_learner_lesson ON playback_sessions(learner_user_id, lesson_id);
CREATE INDEX IF NOT EXISTS idx_ps_expires ON playback_sessions(expires_at);

-- 6. Initial Media Asset Seeding
-- Associate valid media asset references with all seeded lessons
INSERT INTO lesson_media_assets (
    lesson_id, asset_type, storage_provider, storage_object_key,
    duration_seconds, content_version, status, access_classification
)
SELECT
    l.id,
    'VIDEO_STREAM',
    'INTERNAL_STREAMING',
    'media/sample_lesson.mp4',
    COALESCE(l.duration_minutes * 60, 1800),
    '1.0.0',
    'READY',
    CASE WHEN l.is_previewable THEN 'PUBLIC_PREVIEW' ELSE 'ENROLLED_ONLY' END
FROM lessons l
ON CONFLICT (lesson_id, asset_type) DO NOTHING;

-- 7. Grant Permissions to Application User
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'academy_app_user') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE
            learner_course_progress,
            learner_lesson_progress,
            learning_activity_records,
            lesson_media_assets,
            playback_sessions
        TO academy_app_user;
    END IF;
END $$;
