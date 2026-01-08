-- Create onboarding_subjects table
CREATE TABLE onboarding_subjects (
                                     id UUID PRIMARY KEY,
                                     type VARCHAR(50) NOT NULL,
                                     full_name VARCHAR(200) NOT NULL,
                                     email VARCHAR(100) NOT NULL UNIQUE,
                                     phone_number VARCHAR(20),
                                     business_name VARCHAR(100),
                                     tax_id VARCHAR(50),
                                     registration_number VARCHAR(100),
                                     address VARCHAR(500),
                                     city VARCHAR(100),
                                     country VARCHAR(100),
                                     postal_code VARCHAR(20),
                                     created_by VARCHAR(255) NOT NULL,
                                     created_at TIMESTAMP NOT NULL,
                                     updated_at TIMESTAMP NOT NULL,
                                     version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_subjects_email ON onboarding_subjects(email);
CREATE INDEX idx_subjects_type ON onboarding_subjects(type);
CREATE INDEX idx_subjects_created_by ON onboarding_subjects(created_by);
CREATE INDEX idx_subjects_country ON onboarding_subjects(country);

-- Create onboarding_processes table
CREATE TABLE onboarding_processes (
                                      id UUID PRIMARY KEY,
                                      subject_id UUID NOT NULL,
                                      current_state VARCHAR(50) NOT NULL,
                                      correction_attempts INTEGER NOT NULL DEFAULT 0,
                                      max_correction_attempts INTEGER NOT NULL DEFAULT 3,
                                      correction_comments VARCHAR(1000),
                                      verification_passed BOOLEAN,
                                      verification_details VARCHAR(1000),
                                      approved_by VARCHAR(255),
                                      approved_at TIMESTAMP,
                                      approval_comments VARCHAR(1000),
                                      rejected_by VARCHAR(255),
                                      rejected_at TIMESTAMP,
                                      rejection_reason VARCHAR(1000),
                                      submitted_at TIMESTAMP,
                                      completed_at TIMESTAMP,
                                      cancelled_at TIMESTAMP,
                                      cancelled_by VARCHAR(255),
                                      cancellation_reason VARCHAR(500),
                                      created_at TIMESTAMP NOT NULL,
                                      updated_at TIMESTAMP NOT NULL,
                                      version BIGINT NOT NULL DEFAULT 0,
                                      CONSTRAINT fk_process_subject FOREIGN KEY (subject_id) REFERENCES onboarding_subjects(id) ON DELETE CASCADE
);

CREATE INDEX idx_processes_subject_id ON onboarding_processes(subject_id);
CREATE INDEX idx_processes_state ON onboarding_processes(current_state);
CREATE INDEX idx_processes_submitted_at ON onboarding_processes(submitted_at);
CREATE INDEX idx_processes_approved_by ON onboarding_processes(approved_by);
CREATE INDEX idx_processes_completed_at ON onboarding_processes(completed_at);

-- Add comment descriptions
COMMENT ON TABLE onboarding_subjects IS 'Stores identity information for entities being onboarded';
COMMENT ON TABLE onboarding_processes IS 'Tracks workflow state and metadata for onboarding processes';

COMMENT ON COLUMN onboarding_processes.correction_attempts IS 'Number of times process has been sent back for corrections';
COMMENT ON COLUMN onboarding_processes.max_correction_attempts IS 'Maximum allowed correction attempts before automatic rejection';