-- Nexarats Academy Phase 2 Migration
-- Explicit Flyway schema management for Spring Modulith JPA Event Publication Registry.
-- Restores ddl-auto=validate across all environments.

CREATE TABLE IF NOT EXISTS event_publication (
    id UUID PRIMARY KEY,
    listener_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    serialized_event VARCHAR(255) NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    completion_attempts INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(255)
);

-- Ensure check constraint exists safely
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'event_publication_status_check'
    ) THEN
        ALTER TABLE event_publication
        ADD CONSTRAINT event_publication_status_check
        CHECK (status IN ('PUBLISHED', 'PROCESSING', 'COMPLETED', 'FAILED', 'RESUBMITTED'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_event_publication_publication_date ON event_publication(publication_date);
CREATE INDEX IF NOT EXISTS idx_event_publication_status ON event_publication(status);

COMMENT ON TABLE event_publication IS 'Spring Modulith JPA Event Publication Registry for transactional outbox pattern.';
