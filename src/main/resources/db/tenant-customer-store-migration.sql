ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS store_id BIGINT;

WITH customer_stores AS (
    SELECT o.customer_id, MIN(b.store_id) AS store_id
    FROM orders o
    JOIN branches b ON b.id = o.branch_id
    WHERE o.customer_id IS NOT NULL
    GROUP BY o.customer_id
    HAVING COUNT(DISTINCT b.store_id) = 1
)
UPDATE customers c
SET store_id = customer_stores.store_id
FROM customer_stores
WHERE c.id = customer_stores.customer_id
  AND c.store_id IS NULL;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM customers WHERE store_id IS NULL) THEN
        RAISE EXCEPTION 'Assign every customer to a store before completing the tenant migration';
    END IF;
END
$$;

ALTER TABLE customers
    ALTER COLUMN store_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_customers_store'
          AND conrelid = 'customers'::regclass
    ) THEN
        ALTER TABLE customers
            ADD CONSTRAINT fk_customers_store
            FOREIGN KEY (store_id) REFERENCES stores(id);
    END IF;
END
$$;
