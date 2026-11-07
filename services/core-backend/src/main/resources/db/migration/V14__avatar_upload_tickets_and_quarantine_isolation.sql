-- ============================================================================
-- NEXARATS ACADEMY — FLYWAY MIGRATION V14
-- Avatar Upload Tickets, Quarantine Isolation & Overwrite Prevention
-- Phase 6B: Student Profile & Academic Onboarding Final Source-Code Hardening
-- ============================================================================

-- 1. Create avatar_upload_tickets table
-- Binds pre-signed PUT URLs to temporary, isolated quarantine keys and enforces single-use confirmation
CREATE TABLE IF NOT EXISTS avatar_upload_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quarantine_key VARCHAR(512) NOT NULL UNIQUE,
    approved_key VARCHAR(512),
    original_filename VARCHAR(255),
    content_type VARCHAR(64) NOT NULL,
    file_extension VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ISSUED' CHECK (status IN ('ISSUED', 'CONFIRMED', 'EXPIRED', 'QUARANTINED', 'INFECTED')),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_avatar_tickets_user ON avatar_upload_tickets(user_id);
CREATE INDEX IF NOT EXISTS idx_avatar_tickets_tenant ON avatar_upload_tickets(tenant_id);
CREATE INDEX IF NOT EXISTS idx_avatar_tickets_key ON avatar_upload_tickets(quarantine_key);
CREATE INDEX IF NOT EXISTS idx_avatar_tickets_status ON avatar_upload_tickets(status);
