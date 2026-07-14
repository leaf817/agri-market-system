package com.cmh.agrimarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "用户名不能为空") @Size(min = 3, max = 50, message = "用户名长度 3-50") String username,
        @NotBlank(message = "密码不能为空") @Size(min = 6, max = 100, message = "密码长度 6-100") String password,
        String nickname,
        String phone,
        String role,
        Boolean enabled
) {
}
