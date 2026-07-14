package com.cmh.agrimarket.dto;

import com.cmh.agrimarket.entity.OrderStatus;
import jakarta.validation.constraints.NotBlank;

/**
 * 管理端编辑订单：收货信息与状态。
 */
public record UpdateOrderRequest(
        @NotBlank(message = "买家姓名不能为空") String buyerName,
        String buyerPhone,
        String buyerAddress,
        String remark,
        OrderStatus status
) {
}
