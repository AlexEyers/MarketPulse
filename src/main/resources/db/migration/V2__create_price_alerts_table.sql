CREATE TABLE price_alerts(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    symbol VARCHAR(15) NOT NULL,
    condition_type VARCHAR(20) NOT NULL,
    target_price NUMERIC(12,4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_price_alerts_condition_type
        CHECK (condition_type IN ('ABOVE', 'BELOW')),

    CONSTRAINT chk_price_alerts_status
        CHECK (status IN ('ACTIVE', 'DISABLED', 'TRIGGERED', 'EXPIRED')),

    CONSTRAINT chk_price_alerts_target_price_positive
        CHECK (target_price > 0),

    CONSTRAINT chk_price_alerts_expires_after_created
        CHECK (expires_at > created_at),

    CONSTRAINT uq_price_alerts_user_symbol_condition_target_status
        UNIQUE(user_id, symbol, condition_type, target_price, status)
);

CREATE INDEX idx_price_alerts_user_id
    ON price_alerts(user_id);

CREATE INDEX idx_price_alerts_status_symbol_expires_at
    ON price_alerts(status, symbol, expires_at);
