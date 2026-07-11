package com.cmh.agrimarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 消费者自助注册。角色固定为 CONSUMER，不接收前端传入的角色。
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空") @Size(min = 3, max = 50, message = "用户名长度 3-50") String username,
        @NotBlank(message = "密码不能为空") @Size(min = 6, max = 100, message = "密码长度 6-100") String password,
        String nickname
) {
}
