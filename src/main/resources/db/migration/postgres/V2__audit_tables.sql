-- Create audit_events table
CREATE TABLE audit_events (
                              id UUID PRIMARY KEY,
                              process_id UUID NOT NULL,
                              subject_email VARCHAR(100),
                              event_type VARCHAR(50) NOT NULL,
                              description VARCHAR(500),
                              previous_state VARCHAR(50),
                              new_state VARCHAR(50),
                              performed_by VARCHAR(255) NOT NULL,
                              timestamp TIMESTAMP NOT NULL,
                              metadata TEXT,
                              ip_address VARCHAR(45),
                              user_agent VARCHAR(500)
);

-- Indexes for common queries
CREATE INDEX idx_audit_process_id ON audit_events(process_id);
CREATE INDEX idx_audit_event_type ON audit_events(event_type);
CREATE INDEX idx_audit_performed_by ON audit_events(performed_by);
CREATE INDEX idx_audit_timestamp ON audit_events(timestamp);
CREATE INDEX idx_audit_subject_email ON audit_events(subject_email);

-- Composite index for process timeline queries
CREATE INDEX idx_audit_process_timestamp ON audit_events(process_id, timestamp DESC);

-- Index for state transition queries
CREATE INDEX idx_audit_state_transitions ON audit_events(process_id)
    WHERE previous_state IS NOT NULL AND new_state IS NOT NULL;

-- Comments
COMMENT ON TABLE audit_events IS 'Immutable audit log of all onboarding actions';
COMMENT ON COLUMN audit_events.process_id IS 'References onboarding_processes.id';
COMMENT ON COLUMN audit_events.metadata IS 'JSON metadata for additional context';
COMMENT ON COLUMN audit_events.ip_address IS 'IP address of user who performed action';