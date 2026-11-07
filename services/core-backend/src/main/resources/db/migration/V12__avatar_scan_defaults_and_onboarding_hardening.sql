-- ============================================================================
-- NEXARATS ACADEMY — FLYWAY MIGRATION V12
-- Avatar Scan Defaults, Agreement Cryptographic Audit & Onboarding Hardening
-- Phase 6B: Student Profile & Academic Onboarding Final Security Recovery
-- ============================================================================

-- 1. Ensure avatar assets default strictly to PENDING (never automatically CLEAN)
ALTER TABLE avatar_assets ALTER COLUMN scan_status SET DEFAULT 'PENDING';

-- 2. Add immutable content_hash column to student_agreement_acceptances for audit trail
ALTER TABLE student_agreement_acceptances ADD COLUMN IF NOT EXISTS content_hash VARCHAR(64);

-- Backfill content_hash for any pre-existing acceptance records from legal_agreements
UPDATE student_agreement_acceptances saa
SET content_hash = la.content_hash
FROM legal_agreements la
WHERE saa.agreement_id = la.id AND saa.content_hash IS NULL;

-- If any remain null (e.g. orphan), provide fallback empty hash before enforcing NOT NULL
UPDATE student_agreement_acceptances
SET content_hash = 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855'
WHERE content_hash IS NULL;

ALTER TABLE student_agreement_acceptances ALTER COLUMN content_hash SET NOT NULL;

-- 3. Composite index for ultra-fast idempotent acceptance lookups
CREATE INDEX IF NOT EXISTS idx_saa_tenant_user_ag_ver
ON student_agreement_acceptances(tenant_id, student_user_id, agreement_id, agreement_version);
