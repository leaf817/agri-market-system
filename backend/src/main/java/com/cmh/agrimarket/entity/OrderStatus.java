package com.cmh.agrimarket.entity;

public enum OrderStatus {
    PENDING("待付款"),
    PAID("待发货"),
    SHIPPED("已发货"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
