package com.cmh.agrimarket.dto;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        @NotBlank(message = "请输入咨询内容") String message
) {
}
