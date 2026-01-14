-- Create users table
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(500) NOT NULL,
                       full_name VARCHAR(200) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,
                       last_login_at TIMESTAMP,
                       version BIGINT NOT NULL DEFAULT 0
);

-- Indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(active);

-- Comments
COMMENT ON TABLE users IS 'User accounts for authentication and authorization';
COMMENT ON COLUMN users.password_hash IS 'SHA-256 hashed password with salt';
COMMENT ON COLUMN users.role IS 'User role: CUSTOMER, REVIEWER, APPROVER, ADMIN';