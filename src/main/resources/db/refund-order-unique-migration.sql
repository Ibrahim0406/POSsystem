DO $$
BEGIN
    IF EXISTS (
        SELECT order_id
        FROM refunds
        WHERE order_id IS NOT NULL
        GROUP BY order_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Resolve existing duplicate refunds before applying the refund order uniqueness constraint';
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_refunds_order_id'
          AND conrelid = 'refunds'::regclass
    ) THEN
        ALTER TABLE refunds
            ADD CONSTRAINT uk_refunds_order_id UNIQUE (order_id);
    END IF;
END
$$;
