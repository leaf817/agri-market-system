package com.cmh.agrimarket.dto;

public record ShipOrderRequest(
        String deliveryCompany,
        String trackingNo,
        String deliveryRemark
) {
}
