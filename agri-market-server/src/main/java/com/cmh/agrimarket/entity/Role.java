package com.cmh.agrimarket.entity;

/**
 * 用户角色：管理员 / 农户 / 消费者。
 * 数据库存储枚举名（ADMIN/FARMER/CONSUMER），返回前端时转小写。
 */
public enum Role {
    ADMIN,
    FARMER,
    CONSUMER
}
