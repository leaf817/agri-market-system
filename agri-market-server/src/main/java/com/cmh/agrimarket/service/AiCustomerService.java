package com.cmh.agrimarket.service;

import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.dto.AiChatRequest;
import com.cmh.agrimarket.entity.Category;
import com.cmh.agrimarket.entity.Origin;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.repository.CategoryRepository;
import com.cmh.agrimarket.repository.OriginRepository;
import com.cmh.agrimarket.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCustomerService {
    private static final int MAX_HISTORY = 8;
    private static final int MAX_PRODUCTS = 15;

    private final RestClient.Builder restClientBuilder;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OriginRepository originRepository;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:deepseek-v4-flash}")
    private String model;

    @Value("${app.ai.base-url:https://api.deepseek.com/chat/completions}")
    private String baseUrl;

    @Value("${app.ai.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    @Value("${app.ai.read-timeout-ms:8000}")
    private int readTimeoutMs;

    public String chat(String message) {
        return chat(message, List.of());
    }

    public String chat(String message, List<AiChatRequest.ChatMessage> history) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackReply(message);
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", buildMessages(message, history),
                    "stream", false
            );
            Map<?, ?> response = restClientBuilder
                    .requestFactory(aiRequestFactory())
                    .build()
                    .post()
                    .uri(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            String reply = extractReply(response);
            return reply == null || reply.isBlank() ? fallbackReply(message) : reply;
        } catch (Exception e) {
            log.warn("AI customer service request failed, fallback reply will be used: {}", e.getMessage());
            return fallbackReply(message);
        }
    }

    private SimpleClientHttpRequestFactory aiRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return factory;
    }

    private List<Map<String, String>> buildMessages(String message, List<AiChatRequest.ChatMessage> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt()));
        sanitizeHistory(history).forEach(item -> messages.add(Map.of(
                "role", item.role(),
                "content", item.content()
        )));
        messages.add(Map.of("role", "user", "content", message));
        return messages;
    }

    private String buildSystemPrompt() {
        return """
                你是农产品信息展示与销售管理系统的智能客服。
                回答要求：
                1. 使用简洁中文回答，优先结合下方知识库和最近对话上下文。
                2. 可以帮助用户了解商品浏览、购物车、收藏、下单、订单状态、农户上架、后台管理、修改密码。
                3. 不要编造系统中不存在的具体库存、价格、订单或产地信息；不确定时请提示用户到对应页面查看。
                4. 如果用户询问操作路径，请给出页面名称和角色限制。

                知识库：
                %s
                """.formatted(buildKnowledgeBase());
    }

    private String buildKnowledgeBase() {
        CurrentUser current = CurrentUserHolder.get();
        StringBuilder kb = new StringBuilder();
        kb.append("当前用户：")
                .append(current == null ? "未知" : current.nickname() + "（" + roleLabel(current.role()) + "）")
                .append("\n");
        kb.append("角色能力：admin 可管理分类、产地、全部商品、全部订单和统计；farmer 可管理自己的商品、相关订单和统计；consumer 可浏览商品、购物车、收藏、下单、查看自己的订单。\n");
        kb.append("订单状态：PENDING 待付款，PAID 待发货，SHIPPED 已发货，COMPLETED 已完成，CANCELLED 已取消。\n");

        List<Category> categories = categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getSort, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        kb.append("商品分类：");
        kb.append(categories.stream().map(Category::getName).filter(v -> v != null && !v.isBlank()).reduce((a, b) -> a + "、" + b).orElse("暂无"));
        kb.append("\n");

        List<Origin> origins = originRepository.findAll();
        kb.append("产地信息：");
        kb.append(origins.stream()
                .limit(8)
                .map(o -> compact(o.getName()) + "（" + compact(o.getLocation()) + "）")
                .reduce((a, b) -> a + "；" + b)
                .orElse("暂无"));
        kb.append("\n");

        List<Product> products = productRepository.findAll().stream()
                .sorted(Comparator.comparing(Product::getSales, Comparator.nullsLast(Integer::compareTo)).reversed())
                .limit(MAX_PRODUCTS)
                .toList();
        kb.append("可参考商品：\n");
        for (Product p : products) {
            kb.append("- ")
                    .append(compact(p.getName()))
                    .append("，分类：").append(p.getCategory() == null ? "未分类" : compact(p.getCategory().getName()))
                    .append("，价格：").append(p.getPrice()).append("元/")
                    .append(compact(p.getUnit()))
                    .append("，库存：").append(p.getStock())
                    .append("，状态：").append(p.getStatus() != null && p.getStatus() == 1 ? "上架" : "下架")
                    .append("\n");
        }
        return kb.toString();
    }

    private List<AiChatRequest.ChatMessage> sanitizeHistory(List<AiChatRequest.ChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        List<AiChatRequest.ChatMessage> sanitized = history.stream()
                .filter(item -> item != null && item.content() != null && !item.content().isBlank())
                .map(item -> new AiChatRequest.ChatMessage(normalizeRole(item.role()), trim(item.content(), 800)))
                .filter(item -> "user".equals(item.role()) || "assistant".equals(item.role()))
                .toList();
        return sanitized.stream()
                .skip(Math.max(0, sanitized.size() - MAX_HISTORY))
                .toList();
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
            return "您可以在“订单”页面查看订单状态。消费者只能查看自己的订单，农户可以处理包含自己商品的订单，管理员可以查看全部订单。";
        }
        if (text.contains("购物车") || text.contains("加购")) {
            return "消费者可以在商品卡片点击“加入购物车”，然后进入“购物车”页面统一结算。";
        }
        if (text.contains("收藏")) {
            return "消费者可以点击商品上的收藏按钮，之后在“我的收藏”页面快速查看已收藏商品。";
        }
        if (text.contains("上架") || text.contains("商品")) {
            return "管理员和农户可以在“农产品管理”页面新增、编辑、上下架商品，并上传商品封面图。消费者只能浏览和购买上架商品。";
        }
        if (text.contains("密码")) {
            return "您可以进入“个人中心”的“修改密码”模块，填写原密码、新密码和确认密码后保存。";
        }
        return "您好，我是农产品系统智能客服。您可以咨询商品浏览、购物车、收藏、下单、订单状态、修改密码或农户上架等问题。";
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "user";
        }
        return "assistant".equalsIgnoreCase(role) ? "assistant" : "user";
    }

    private String roleLabel(Role role) {
        if (role == Role.ADMIN) {
            return "管理员";
        }
        if (role == Role.FARMER) {
            return "农户";
        }
        if (role == Role.CONSUMER) {
            return "消费者";
        }
        return "未知角色";
    }

    private String compact(String value) {
        return value == null || value.isBlank() ? "未填写" : value.trim();
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
