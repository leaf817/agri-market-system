package com.cmh.agrimarket.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AiChatRequest(
        @NotBlank(message = "请输入咨询内容") String message,
        List<ChatMessage> history
) {
    public record ChatMessage(
            String role,
            String content
    ) {
    }
}
