package com.cmh.agrimarket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "买家姓名不能为空") String buyerName,
        String buyerPhone,
        String buyerAddress,
        String remark,
        @NotEmpty(message = "订单至少包含一件商品") @Valid List<OrderItemRequest> items
) {
}
