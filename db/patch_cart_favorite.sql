-- 增量：为已有库补充购物车 / 收藏表（可重复执行）
USE agrimarket;

CREATE TABLE IF NOT EXISTS cart_item (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    product_id  BIGINT      NOT NULL,
    quantity    INT         NOT NULL DEFAULT 1,
    create_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_user_product (user_id, product_id),
    KEY idx_cart_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS favorite (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    product_id  BIGINT      NOT NULL,
    create_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_favorite_user_product (user_id, product_id),
    KEY idx_favorite_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
