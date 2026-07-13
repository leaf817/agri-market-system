package com.cmh.agrimarket.dto;

public record UpdateProfileRequest(
        String nickname,
        String phone,
        String avatar,
        String address
) {
}