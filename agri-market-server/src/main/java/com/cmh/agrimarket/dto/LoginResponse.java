package com.cmh.agrimarket.dto;

public record LoginResponse(
        String token,
        UserVO user
) {
}
