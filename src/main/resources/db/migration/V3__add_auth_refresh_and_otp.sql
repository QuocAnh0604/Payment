ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(255);

CREATE TABLE refresh_tokens (
    token_id    VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL REFERENCES users(user_id),
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    revoked_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expiry ON refresh_tokens(expires_at);

CREATE TABLE otp_challenges (
    otp_id         UUID PRIMARY KEY,
    user_id        VARCHAR(36) NOT NULL REFERENCES users(user_id),
    purpose        VARCHAR(20) NOT NULL CHECK (purpose IN ('TOP_UP', 'TRANSFER', 'LOGIN')),
    reference_id   VARCHAR(64) NOT NULL,
    channel        VARCHAR(10) NOT NULL CHECK (channel IN ('SMS', 'EMAIL')),
    recipient_hint VARCHAR(255),
    otp_hash       VARCHAR(128) NOT NULL,
    expires_at     TIMESTAMP NOT NULL,
    attempt_count  INT NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
    max_attempts   INT NOT NULL DEFAULT 5 CHECK (max_attempts > 0),
    used_at        TIMESTAMP,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_otp_expiry CHECK (expires_at > created_at)
);
CREATE INDEX idx_otp_reference ON otp_challenges(purpose, reference_id);
CREATE INDEX idx_otp_user ON otp_challenges(user_id);
