-- Keep currency values free of CHAR padding and align all existing schemas.
ALTER TABLE wallets
    ALTER COLUMN currency TYPE VARCHAR(3)
    USING TRIM(currency)::VARCHAR(3);

ALTER TABLE cards
    ALTER COLUMN last4 TYPE VARCHAR(4)
    USING TRIM(last4)::VARCHAR(4);

ALTER TABLE payment_orders
    ALTER COLUMN currency TYPE VARCHAR(3)
    USING TRIM(currency)::VARCHAR(3);

ALTER TABLE top_up_orders
    ALTER COLUMN currency TYPE VARCHAR(3)
    USING TRIM(currency)::VARCHAR(3);

ALTER TABLE ledger_entries
    ALTER COLUMN currency TYPE VARCHAR(3)
    USING TRIM(currency)::VARCHAR(3);
