package com.cmh.agrimarket.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易 Token 服务：内存保存 token -> userId。重启即失效，满足实训需求。
 */
@Service
public class TokenService {
    private final Map<String, Long> store = new ConcurrentHashMap<>();

    public String issue(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        store.put(token, userId);
        return token;
    }

    public Long resolve(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return store.get(token);
    }

    public void remove(String token) {
        if (token != null) {
            store.remove(token);
        }
    }
}
