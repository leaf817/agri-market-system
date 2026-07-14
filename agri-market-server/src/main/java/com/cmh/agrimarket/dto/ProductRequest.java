package com.cmh.agrimarket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "商品名称不能为空") String name,
        Long categoryId,
        @NotNull(message = "请选择产地") Long originId,
        @NotNull @DecimalMin(value = "0", message = "价格不能小于0") BigDecimal price,
        @NotNull @Min(value = 0, message = "库存不能小于0") Integer stock,
        String unit,
        String cover,
        String description,
        Integer status
) {
}
