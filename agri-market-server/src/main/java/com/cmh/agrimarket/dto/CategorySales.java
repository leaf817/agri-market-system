package com.cmh.agrimarket.dto;

import java.math.BigDecimal;

/** 按品类汇总的销量/销售额 */
public record CategorySales(
        Long categoryId,
        String categoryName,
        long quantity,
        BigDecimal amount
) {
}
