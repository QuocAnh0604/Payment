CREATE TABLE top_up_orders (
    top_up_order_id VARCHAR(64) PRIMARY KEY,
    user_id         VARCHAR(36) NOT NULL REFERENCES users(user_id),
    card_id         VARCHAR(36) NOT NULL REFERENCES cards(card_id),
    wallet_id       VARCHAR(36) NOT NULL REFERENCES wallets(wallet_id),
    amount          NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    currency        CHAR(3) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    failure_reason  VARCHAR(255),
    wallet_updated  BOOLEAN NOT NULL DEFAULT FALSE,
    ledger_updated  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE ledger_entries
    ALTER COLUMN payment_order_id DROP NOT NULL;

ALTER TABLE ledger_entries
    ADD COLUMN top_up_order_id VARCHAR(64) REFERENCES top_up_orders(top_up_order_id);

ALTER TABLE ledger_entries
    ADD CONSTRAINT chk_ledger_source
    CHECK ((payment_order_id IS NOT NULL AND top_up_order_id IS NULL)
        OR (payment_order_id IS NULL AND top_up_order_id IS NOT NULL));

CREATE INDEX idx_ledger_top_up_order ON ledger_entries(top_up_order_id);
