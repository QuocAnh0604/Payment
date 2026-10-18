ALTER TABLE ledger_entries
    ALTER COLUMN currency TYPE VARCHAR(3)
    USING TRIM(currency)::VARCHAR(3);

ALTER TABLE payment_orders
    ALTER COLUMN currency TYPE VARCHAR(3)
    USING TRIM(currency)::VARCHAR(3);

ALTER TABLE top_up_orders
    ALTER COLUMN currency TYPE VARCHAR(3)
    USING TRIM(currency)::VARCHAR(3);
