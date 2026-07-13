package com.cmh.agrimarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiCustomerService {
    private final RestClient.Builder restClientBuilder;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:deepseek-v4-flash}")
    private String model;

    @Value("${app.ai.base-url:https://api.deepseek.com/chat/completions}")
    private String baseUrl;

    public String chat(String message) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackReply(message);
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "你是农产品信息展示与销售管理系统的客服。请用中文简洁回答，帮助用户了解商品、购物车、收藏、下单、订单状态、农户上架和后台管理。不要编造系统中不存在的具体库存或价格。"
                            ),
                            Map.of("role", "user", "content", message)
                    ),
                    "stream", false
            );
            Map<?, ?> response = restClientBuilder.build().post()
                    .uri(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            String reply = extractReply(response);
            return reply == null || reply.isBlank() ? fallbackReply(message) : reply;
        } catch (Exception e) {
            return fallbackReply(message);
        }
    }

    private String extractReply(Map<?, ?> response) {
        if (response == null) {
            return null;
        }
        Object outputText = response.get("output_text");
        if (outputText instanceof String text) {
            return text;
        }
        Object choices = response.get("choices");
        if (choices instanceof List<?> choiceList) {
            for (Object choice : choiceList) {
                if (choice instanceof Map<?, ?> choiceMap && choiceMap.get("message") instanceof Map<?, ?> messageMap) {
                    Object content = messageMap.get("content");
                    if (content instanceof String text) {
                        return text;
                    }
                }
            }
        }
        Object output = response.get("output");
        if (output instanceof List<?> outputList) {
            for (Object item : outputList) {
                if (item instanceof Map<?, ?> itemMap && itemMap.get("content") instanceof List<?> contentList) {
                    for (Object content : contentList) {
                        if (content instanceof Map<?, ?> contentMap && contentMap.get("text") instanceof String text) {
                            return text;
                        }
                    }
                }
            }
        }
        return null;
    }

    private String fallbackReply(String message) {
        String text = message == null ? "" : message;
        if (text.contains("订单")) {
            return "您可以在“订单”页面查看订单状态。消费者可以查看自己的订单，农户和管理员可以处理相关订单。";
        }
        if (text.contains("购物车") || text.contains("加购")) {
            return "消费者可以在商品卡片点击“加入购物车”，然后进入“购物车”统一结算。";
        }
        if (text.contains("收藏")) {
            return "消费者可以点击商品上的收藏按钮，之后在“我的收藏”页面快速查看已收藏商品。";
        }
        if (text.contains("上架") || text.contains("商品")) {
            return "管理员和农户可以在农产品管理页面新增、编辑、上下架商品，并上传商品封面图。";
        }
        if (text.contains("密码")) {
            return "您可以进入“个人中心”的“修改密码”模块，填写原密码、新密码和确认密码后保存。";
        }
        return "您好，我是农产品系统客服。您可以咨询商品浏览、购物车、收藏、下单、订单状态、修改密码或农户上架等问题。";
    }
}
