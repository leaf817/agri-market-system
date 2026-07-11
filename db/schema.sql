-- ============================================================
-- 农产品信息展示与售卖管理系统 - 数据库结构脚本（schema）
-- 用法：mysql -u root -p < db/schema.sql
-- 注意：会删除并重建 agrimarket 库，已有数据将丢失！
-- ============================================================

DROP DATABASE IF EXISTS agrimarket;
CREATE DATABASE agrimarket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agrimarket;

DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS origin;
DROP TABLE IF EXISTS category;

-- 商品分类
CREATE TABLE category (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(200) NULL,
    sort        INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 产地信息公示
CREATE TABLE origin (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)  NOT NULL COMMENT '产地/基地名称',
    location    VARCHAR(200)  NULL COMMENT '所在地',
    farmer      VARCHAR(100)  NULL COMMENT '农户/合作社',
    phone       VARCHAR(30)   NULL,
    certificate VARCHAR(200)  NULL COMMENT '资质认证',
    description VARCHAR(1000) NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 农产品
CREATE TABLE product (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)   NOT NULL,
    category_id BIGINT         NULL,
    origin_id   BIGINT         NULL,
    price       DECIMAL(10, 2) NOT NULL,
    stock       INT            NOT NULL DEFAULT 0,
    unit        VARCHAR(20)    NULL,
    cover       VARCHAR(500)   NULL COMMENT '封面图 URL',
    description VARCHAR(1000)  NULL,
    sales       INT            NOT NULL DEFAULT 0 COMMENT '累计销量',
    status      INT            NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    create_time DATETIME(6)    NULL,
    PRIMARY KEY (id),
    KEY idx_product_category (category_id),
    KEY idx_product_origin (origin_id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_product_origin FOREIGN KEY (origin_id) REFERENCES origin (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 订单（表名用 orders，避开关键字 order）
CREATE TABLE orders (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    order_no     VARCHAR(32)    NOT NULL,
    buyer_name   VARCHAR(50)    NOT NULL,
    buyer_phone  VARCHAR(30)    NULL,
    buyer_address VARCHAR(200)  NULL,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    status       VARCHAR(20)    NOT NULL,
    remark       VARCHAR(500)   NULL,
    create_time  DATETIME(6)    NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_no (order_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 订单明细
CREATE TABLE order_item (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    order_id     BIGINT         NULL,
    product_id   BIGINT         NULL,
    product_name VARCHAR(100)   NOT NULL COMMENT '商品名称快照',
    price        DECIMAL(10, 2) NOT NULL COMMENT '成交单价快照',
    quantity     INT            NOT NULL,
    amount       DECIMAL(12, 2) NOT NULL COMMENT '小计',
    PRIMARY KEY (id),
    KEY idx_item_order (order_id),
    KEY idx_item_product (product_id),
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
