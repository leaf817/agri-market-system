-- 增量：为订单增加模拟支付、发货、完成、取消流程字段（可重复执行）
USE agrimarket;

DROP PROCEDURE IF EXISTS add_order_flow_column;
DELIMITER //
CREATE PROCEDURE add_order_flow_column(IN col_name VARCHAR(64), IN col_def VARCHAR(255))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'orders'
          AND COLUMN_NAME = col_name
    ) THEN
        SET @sql = CONCAT('ALTER TABLE orders ADD COLUMN ', col_name, ' ', col_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

CALL add_order_flow_column('pay_time', 'DATETIME(6) NULL');
CALL add_order_flow_column('ship_time', 'DATETIME(6) NULL');
CALL add_order_flow_column('complete_time', 'DATETIME(6) NULL');
CALL add_order_flow_column('cancel_time', 'DATETIME(6) NULL');
CALL add_order_flow_column('delivery_company', 'VARCHAR(80) NULL');
CALL add_order_flow_column('tracking_no', 'VARCHAR(80) NULL');
CALL add_order_flow_column('delivery_remark', 'VARCHAR(500) NULL');
CALL add_order_flow_column('cancel_reason', 'VARCHAR(500) NULL');

DROP PROCEDURE IF EXISTS add_order_flow_column;
