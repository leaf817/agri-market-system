-- 增量：补充农户产地归属，支持农户自有产地与自有商品管理（可重复执行）
USE agrimarket;

DROP PROCEDURE IF EXISTS add_origin_farmer_id;
DELIMITER //
CREATE PROCEDURE add_origin_farmer_id()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'origin'
          AND COLUMN_NAME = 'farmer_id'
    ) THEN
        ALTER TABLE origin ADD COLUMN farmer_id BIGINT NULL;
        CREATE INDEX idx_origin_farmer ON origin (farmer_id);
    END IF;
END//
DELIMITER ;

CALL add_origin_farmer_id();
DROP PROCEDURE IF EXISTS add_origin_farmer_id;

UPDATE origin
SET farmer_id = (SELECT id FROM user WHERE username = 'farmer' LIMIT 1)
WHERE farmer_id IS NULL;

UPDATE product
SET farmer_id = (SELECT id FROM user WHERE username = 'farmer' LIMIT 1)
WHERE farmer_id IS NULL;
