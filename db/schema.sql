-- ============================================================
-- 农产品信息展示与售卖管理系统 - 数据库结构脚本（schema）
-- 用法：mysql -u root -p < db/schema.sql
-- 注意：会删除并重建 agrimarket 库，已有数据将丢失！
-- ============================================================

DROP DATABASE IF EXISTS agrimarket;
CREATE DATABASE agrimarket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agrimarket;

DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS origin;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS user;

-- 用户（管理员 / 农户 / 消费者）
CREATE TABLE user (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 哈希',
    nickname    VARCHAR(50)  NULL,
    phone       VARCHAR(15)  NULL COMMENT '手机号',
    avatar      VARCHAR(500) NULL COMMENT '头像 URL',
    address     VARCHAR(500) NULL COMMENT '收货地址',
    role        VARCHAR(20)  NOT NULL COMMENT 'ADMIN/FARMER/CONSUMER',
    enabled     TINYINT(1)   NOT NULL DEFAULT 1,
    create_time DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

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
    farmer_id   BIGINT         NULL COMMENT '所属农户用户 id（admin 公共产品为 null）',
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
    KEY idx_product_farmer (farmer_id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_product_origin FOREIGN KEY (origin_id) REFERENCES origin (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 订单（表名用 orders，避开关键字 order）
CREATE TABLE orders (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    order_no     VARCHAR(32)    NOT NULL,
    customer_id  BIGINT         NULL COMMENT '下单消费者用户 id',
    buyer_name   VARCHAR(50)    NOT NULL,
    buyer_phone  VARCHAR(30)    NULL,
    buyer_address VARCHAR(200)  NULL,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    status       VARCHAR(20)    NOT NULL,
    remark       VARCHAR(500)   NULL,
    create_time  DATETIME(6)    NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_no (order_no),
    KEY idx_orders_customer (customer_id)
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

-- 购物车
CREATE TABLE cart_item (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    product_id  BIGINT       NOT NULL,
    quantity    INT          NOT NULL DEFAULT 1,
    create_time DATETIME(6)  NULL,
    update_time DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_cart_user (user_id),
    KEY idx_cart_product (product_id),
    UNIQUE KEY uk_cart_user_product (user_id, product_id),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 商品评价
CREATE TABLE review (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    user_id       BIGINT         NOT NULL,
    order_item_id BIGINT         NULL,
    product_id    BIGINT         NOT NULL,
    rating        INT            NOT NULL COMMENT '1-5分',
    content       VARCHAR(1000)  NULL,
    create_time   DATETIME(6)    NULL,
    PRIMARY KEY (id),
    KEY idx_review_user (user_id),
    KEY idx_review_product (product_id),
    KEY idx_review_order_item (order_item_id),
    CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_review_order_item FOREIGN KEY (order_item_id) REFERENCES order_item (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 收藏
CREATE TABLE favorite (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    product_id  BIGINT       NOT NULL,
    create_time DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_favorite_user (user_id),
    KEY idx_favorite_product (product_id),
    UNIQUE KEY uk_favorite_user_product (user_id, product_id),
    CONSTRAINT fk_favorite_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 收货地址
CREATE TABLE address (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    name        VARCHAR(50)  NOT NULL COMMENT '收货人',
    phone       VARCHAR(15)  NOT NULL COMMENT '手机号',
    address     VARCHAR(500) NOT NULL COMMENT '详细地址',
    is_default  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否默认地址',
    create_time DATETIME(6)  NULL,
    update_time DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_address_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
