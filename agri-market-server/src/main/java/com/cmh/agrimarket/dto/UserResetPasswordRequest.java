package com.cmh.agrimarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserResetPasswordRequest(
        @NotBlank(message = "新密码不能为空") @Size(min = 6, max = 100, message = "密码长度 6-100") String newPassword
) {
}
