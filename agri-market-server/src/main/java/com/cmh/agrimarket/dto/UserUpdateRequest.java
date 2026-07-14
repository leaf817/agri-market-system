package com.cmh.agrimarket.dto;

public record UserUpdateRequest(
        String nickname,
        String phone,
        String avatar,
        String address,
        String role,
        Boolean enabled
) {
}
