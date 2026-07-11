package com.cmh.agrimarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CartCheckoutRequest(
        @NotEmpty(message = "请选择要结算的商品") List<Long> cartItemIds,
        @NotBlank(message = "买家姓名不能为空") String buyerName,
        String buyerPhone,
        String buyerAddress,
        String remark
) {
}
