package com.cmh.agrimarket.dto;

import java.math.BigDecimal;

/** 销量 Top 商品 */
public record TopProduct(
        Long productId,
        String name,
        Integer sales,
        BigDecimal amount
) {
}
