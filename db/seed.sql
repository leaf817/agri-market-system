-- ============================================================
-- 农产品信息展示与售卖管理系统 - 演示数据脚本（seed）
-- 前提：库表已存在（已执行 schema.sql，或后端以 ddl-auto=update 启动后自动建表）
-- 用法：mysql -u root -p agrimarket < db/seed.sql
-- 特性：可重复执行 —— 先清空业务表再插入；订单不预置，可在前端「模拟下单」体验
-- ============================================================

USE agrimarket;

-- 清空业务数据（TRUNCATE 顺带重置自增；临时关闭外键检查避免顺序约束）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_item;
TRUNCATE TABLE orders;
TRUNCATE TABLE product;
TRUNCATE TABLE origin;
TRUNCATE TABLE category;
SET FOREIGN_KEY_CHECKS = 1;

-- 分类
INSERT INTO category (id, name, description, sort) VALUES
(1, '新鲜水果', '应季水果', 1),
(2, '绿色蔬菜', '无公害蔬菜', 2),
(3, '粮油米面', '农家自产粮油', 3),
(4, '乡土特产', '地方特色农产品', 4);

-- 产地公示
INSERT INTO origin (id, name, location, farmer, phone, certificate, description) VALUES
(1, '青山生态果园', '浙江省丽水市青田村', '张大为', '13800001111', '绿色食品认证', '高山果园，昼夜温差大，自然成熟不催熟。'),
(2, '稻香村合作社', '黑龙江省五常市', '李合作社', '13900002222', '有机认证', '黑土地种植，稻花香米核心产区。'),
(3, '云岭蔬菜基地', '云南省昆明市呈贡区', '王阿姨', '13700003333', '无公害认证', '高原阳光充足，蔬菜清脆甘甜。');

-- 农产品（sales 为演示初始销量；在前端下单后会累加）
INSERT INTO product (id, name, category_id, origin_id, price, stock, unit, cover, description, sales, status, create_time) VALUES
(1, '赣南脐橙', 1, 1,  8.80, 200, '斤', NULL, '皮薄多汁、酸甜可口，现摘现发。', 156, 1, NOW()),
(2, '烟台红富士苹果', 1, 1,  6.50, 150, '斤', NULL, '脆甜爽口，冰糖心。', 98, 1, NOW()),
(3, '五常稻花香大米', 3, 2, 12.90, 300, '斤', NULL, '黑土地稻花香，软糯香甜。', 230, 1, NOW()),
(4, '农家笨榨花生油', 3, 2, 35.00,  80, '桶', NULL, '传统工艺压榨，香味浓郁。', 64, 1, NOW()),
(5, '高山娃娃菜', 2, 3,  3.50, 120, '棵', NULL, '高原种植，清脆无筋。', 45, 1, NOW()),
(6, '铁棍山药', 2, 3,  9.90,  90, '斤', NULL, '粉糯香甜，营养丰富。', 72, 1, NOW()),
(7, '手工红薯粉条', 4, 1, 15.00,  60, '斤', NULL, '纯红薯制作，久煮不烂。', 38, 1, NOW());

-- 显式抬高自增起点，避免后续从页面新增时与演示数据主键冲突
ALTER TABLE category AUTO_INCREMENT = 5;
ALTER TABLE origin   AUTO_INCREMENT = 4;
ALTER TABLE product  AUTO_INCREMENT = 8;
