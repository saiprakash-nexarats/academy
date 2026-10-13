-- Nexarats Academy Phase 2 Migration
-- Relaxes foreign key constraints on immutable audit log table.
-- Ensures security audit events can always be recorded independently of entity lifecycles.

ALTER TABLE auth_audit_events DROP CONSTRAINT IF EXISTS auth_audit_events_user_id_fkey;
ALTER TABLE auth_audit_events DROP CONSTRAINT IF EXISTS auth_audit_events_tenant_id_fkey;
