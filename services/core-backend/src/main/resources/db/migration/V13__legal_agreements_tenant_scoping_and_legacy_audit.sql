-- ============================================================================
-- NEXARATS ACADEMY — FLYWAY MIGRATION V13
-- Tenant-Scoped Agreement Versioning & Cryptographic Audit Legacy Flagging
-- Phase 6B: Student Profile & Academic Onboarding Final Acceptance Closure
-- ============================================================================

-- 1. Drop global uniqueness constraint that prevented multiple tenants from using same version
ALTER TABLE legal_agreements DROP CONSTRAINT IF EXISTS uq_legal_agreements_type_version;

-- 2. Tenant-scoped agreement uniqueness: each tenant can define their own version strings
CREATE UNIQUE INDEX IF NOT EXISTS uq_legal_agreements_tenant_type_version
ON legal_agreements(tenant_id, agreement_type, version)
WHERE tenant_id IS NOT NULL;

-- 3. Canonical global agreements uniqueness: exactly one active version per agreement type when tenant_id is NULL
CREATE UNIQUE INDEX IF NOT EXISTS uq_legal_agreements_global_type_version
ON legal_agreements(agreement_type, version)
WHERE tenant_id IS NULL;

-- 4. Add explicit is_legacy_unverified flag to student_agreement_acceptances
ALTER TABLE student_agreement_acceptances ADD COLUMN IF NOT EXISTS is_legacy_unverified BOOLEAN NOT NULL DEFAULT FALSE;

-- 5. Quarantine legacy orphan records: do not treat empty-string hashes as valid acceptance evidence
UPDATE student_agreement_acceptances
SET is_legacy_unverified = TRUE,
    content_hash = 'UNVERIFIABLE_LEGACY_RECORD'
WHERE content_hash = 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855'
   OR content_hash IS NULL;

