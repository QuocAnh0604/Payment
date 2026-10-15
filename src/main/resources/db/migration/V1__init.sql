-- ========== USER ==========
CREATE TABLE users (
    user_id        VARCHAR(36)  PRIMARY KEY,
    full_name      VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    phone          VARCHAR(20),
    national_id    VARCHAR(255),
    address        VARCHAR(500),
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMP    NOT NULL DEFAULT now()
);

-- ========== CARD ==========
CREATE TABLE cards (
    card_id        VARCHAR(36)  PRIMARY KEY,
    user_id        VARCHAR(36)  NOT NULL REFERENCES users(user_id),
    issuer         VARCHAR(100) NOT NULL,
    card_token     VARCHAR(255) NOT NULL UNIQUE,
    last4          CHAR(4)      NOT NULL,
    card_type      VARCHAR(10)  NOT NULL,
    issue_date     DATE,
    expiry_date    DATE         NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_cards_user ON cards(user_id);

-- ========== WALLET ==========
CREATE TABLE wallets (
    wallet_id      VARCHAR(36)   PRIMARY KEY,
    card_id        VARCHAR(36)   NOT NULL UNIQUE REFERENCES cards(card_id),
    balance        NUMERIC(19,4) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    currency       CHAR(3)       NOT NULL,
    version        BIGINT        NOT NULL DEFAULT 0
);

-- ========== PAYMENT EVENT ==========
CREATE TABLE payment_events (
    checkout_id     VARCHAR(64) PRIMARY KEY,
    user_id         VARCHAR(36) NOT NULL REFERENCES users(user_id),
    is_payment_done BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now()
);

-- ========== PAYMENT ORDER ==========
CREATE TABLE payment_orders (
    payment_order_id     VARCHAR(64)   PRIMARY KEY,
    checkout_id          VARCHAR(64)   NOT NULL REFERENCES payment_events(checkout_id),
    source_card_id       VARCHAR(36)   NOT NULL REFERENCES cards(card_id),
    destination_card_id  VARCHAR(36)   NOT NULL REFERENCES cards(card_id),
    amount               NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    currency             CHAR(3)       NOT NULL,
    payment_order_status VARCHAR(20)   NOT NULL,
    failure_reason       VARCHAR(255),
    ledger_updated       BOOLEAN       NOT NULL DEFAULT FALSE,
    wallet_updated       BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT chk_diff_card CHECK (source_card_id <> destination_card_id)
);
CREATE INDEX idx_orders_checkout ON payment_orders(checkout_id);

-- ========== LEDGER ENTRY (append-only) ==========
CREATE TABLE ledger_entries (
    entry_id         BIGSERIAL     PRIMARY KEY,
    payment_order_id VARCHAR(64)   NOT NULL REFERENCES payment_orders(payment_order_id),
    card_id          VARCHAR(36)   NOT NULL REFERENCES cards(card_id),
    entry_type       VARCHAR(6)    NOT NULL CHECK (entry_type IN ('DEBIT','CREDIT')),
    amount           NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    currency         CHAR(3)       NOT NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT now()
);
CREATE INDEX idx_ledger_card  ON ledger_entries(card_id);
CREATE INDEX idx_ledger_order ON ledger_entries(payment_order_id);

CREATE FUNCTION forbid_ledger_change() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'ledger_entries is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ledger_immutable
BEFORE UPDATE OR DELETE ON ledger_entries
FOR EACH ROW EXECUTE FUNCTION forbid_ledger_change();
