-- ============================================================================
-- NEXARATS ACADEMY — FLYWAY MIGRATION V11
-- Student Profiles, Legal Agreements, E-Signatures & Avatar Assets
-- Phase 6B: Student Profile & Academic Onboarding
-- ============================================================================

-- 1. Avatar Assets Table
-- Tracks student profile picture uploads, MIME magic bytes verification & security scans
CREATE TABLE IF NOT EXISTS avatar_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    storage_object_key VARCHAR(512) NOT NULL UNIQUE,
    original_filename VARCHAR(255),
    content_type VARCHAR(64) NOT NULL,
    file_size_bytes BIGINT NOT NULL CHECK (file_size_bytes > 0 AND file_size_bytes <= 5242880),
    scan_status VARCHAR(32) NOT NULL DEFAULT 'CLEAN' CHECK (scan_status IN ('PENDING', 'CLEAN', 'QUARANTINED', 'INFECTED')),
    scan_details TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    scanned_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_avatar_assets_user ON avatar_assets(user_id);
CREATE INDEX IF NOT EXISTS idx_avatar_assets_tenant ON avatar_assets(tenant_id);
CREATE INDEX IF NOT EXISTS idx_avatar_assets_status ON avatar_assets(scan_status);

-- 2. Student Profiles Table
-- Enriched academic profile, developer links, timezone and onboarding state
CREATE TABLE IF NOT EXISTS student_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    headline VARCHAR(255),
    bio TEXT,
    academic_background TEXT,
    career_goals TEXT,
    timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    github_url VARCHAR(255),
    linkedin_url VARCHAR(255),
    avatar_asset_id UUID REFERENCES avatar_assets(id) ON DELETE SET NULL,
    onboarding_status VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED' CHECK (onboarding_status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
    onboarding_completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_profiles_tenant_user UNIQUE (tenant_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_student_profiles_user ON student_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_student_profiles_tenant ON student_profiles(tenant_id);
CREATE INDEX IF NOT EXISTS idx_student_profiles_status ON student_profiles(onboarding_status);

-- 3. Authoritative Legal Agreements Table
-- Versioned legal documents (Terms of Service, Academic Integrity, Code of Conduct)
CREATE TABLE IF NOT EXISTS legal_agreements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    agreement_type VARCHAR(64) NOT NULL CHECK (agreement_type IN ('TERMS_OF_SERVICE', 'ACADEMIC_INTEGRITY', 'CODE_OF_CONDUCT')),
    version VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    effective_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_legal_agreements_type_version UNIQUE (agreement_type, version)
);

CREATE INDEX IF NOT EXISTS idx_legal_agreements_type ON legal_agreements(agreement_type, is_active);
CREATE INDEX IF NOT EXISTS idx_legal_agreements_tenant ON legal_agreements(tenant_id);

-- 4. Student Agreement Acceptances Table
-- Non-repudiable audit trail of agreement e-signatures
CREATE TABLE IF NOT EXISTS student_agreement_acceptances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    student_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    agreement_id UUID NOT NULL REFERENCES legal_agreements(id) ON DELETE CASCADE,
    agreement_version VARCHAR(32) NOT NULL,
    accepted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(64),
    user_agent TEXT,
    acceptance_method VARCHAR(32) NOT NULL DEFAULT 'CLICKWRAP_CHECKBOX',
    CONSTRAINT uq_student_agreement_acceptance UNIQUE (tenant_id, student_user_id, agreement_id, agreement_version)
);

CREATE INDEX IF NOT EXISTS idx_saa_student_user ON student_agreement_acceptances(student_user_id);
CREATE INDEX IF NOT EXISTS idx_saa_tenant ON student_agreement_acceptances(tenant_id);
CREATE INDEX IF NOT EXISTS idx_saa_agreement ON student_agreement_acceptances(agreement_id);

-- 5. Seed Canonical Legal Agreements (Version 1.0)
INSERT INTO legal_agreements (
    id, tenant_id, agreement_type, version, title, content, content_hash, is_active, created_at
) VALUES
(
    '10000000-0000-0000-0000-000000000001',
    NULL,
    'TERMS_OF_SERVICE',
    '1.0',
    'Nexarats Academy Terms of Service',
    'Welcome to Nexarats Academy. By accessing or using our learning platform, you agree to be bound by these Terms of Service. All course materials, video lectures, code repositories, assessments, and learning assets are proprietary to Nexarats Academy. You are granted a limited, personal, non-exclusive, non-transferable license to access course content solely for your individual educational purposes. Account sharing, unauthorized downloading, reproduction, re-hosting, or redistribution of curriculum material is strictly prohibited and subject to immediate revocation of access without refund.',
    '6f91e9a28055cab586559828536864da47b022cbe58d2c061f3ed766d57220c4',
    TRUE,
    CURRENT_TIMESTAMP
),
(
    '10000000-0000-0000-0000-000000000002',
    NULL,
    'ACADEMIC_INTEGRITY',
    '1.0',
    'Nexarats Academy Academic Integrity Agreement',
    'As a learner enrolled in Nexarats Academy programs, you commit to upholding the highest standards of academic honesty and ethical conduct. All assignments, capstone projects, code submissions, and examination responses must represent your own original work. Collaboration is encouraged during designated group workshops, but plagiarism, code duplication without attribution, sharing solutions to graded evaluations, or misrepresenting automated tools as unassisted work constitutes an academic integrity violation resulting in disciplinary review or expulsion.',
    'a6b1adb495692064419c3699aec5f4d9e29d6649e5242d3bb66ff32284ea5c7e',
    TRUE,
    CURRENT_TIMESTAMP
),
(
    '10000000-0000-0000-0000-000000000003',
    NULL,
    'CODE_OF_CONDUCT',
    '1.0',
    'Nexarats Academy Community Code of Conduct',
    'Nexarats Academy is committed to providing a safe, inclusive, welcoming, and harassment-free environment for all learners, mentors, instructors, and staff regardless of background, gender identity, sexual orientation, disability, physical appearance, race, ethnicity, or religion. Harassment, derogatory comments, disruptive conduct in community channels or live sessions, and unprofessional behavior will not be tolerated. Violations may result in formal warnings, temporary suspension, or permanent termination of community privileges.',
    'a3db5e6bc53de7c2df3ec07f7849e34a6e5c8270c15cee8038353d2f8d9c486a',
    TRUE,
    CURRENT_TIMESTAMP
)
ON CONFLICT (agreement_type, version) DO NOTHING;
