package com.cmh.agrimarket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "商品ID不能为空") Long productId,
        @NotNull @Min(value = 1, message = "购买数量至少为1") Integer quantity
) {
}
