package com.cmh.agrimarket.common;

import lombok.Getter;

/**
 * 鉴权相关异常：401 未登录 / 403 无权限。由 GlobalExceptionHandler 统一包装为 ApiResponse。
 */
@Getter
public class AuthException extends RuntimeException {
    private final int status;

    public AuthException(int status, String message) {
        super(message);
        this.status = status;
    }
}
