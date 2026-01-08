ALTER TABLE orders
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'PENDING';

-- If you want to be extra safe/explicit:
UPDATE orders
SET status = 'PENDING'
WHERE status IS NULL;
