package com.cmh.agrimarket.dto;

import java.math.BigDecimal;

/** 经营总览 */
public record StatsOverview(
        long productCount,
        long orderCount,
        long totalQuantity,
        BigDecimal totalAmount
) {
}
