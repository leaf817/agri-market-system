package com.cmh.agrimarket.service;

import com.cmh.agrimarket.entity.AuthToken;
import com.cmh.agrimarket.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Token 服务：写入 MySQL，后端重启后登录态仍有效。
 */
@Service
@RequiredArgsConstructor
public class TokenService {
    /** 默认有效期 7 天 */
    private static final long DAYS_VALID = 7;

    private final AuthTokenRepository repo;

    @Transactional
    public String issue(Long userId) {
        // 新登录使旧 token 失效，避免多端永久有效与表膨胀
        repo.deleteByUserId(userId);
        String token = UUID.randomUUID().toString().replace("-", "");
        AuthToken row = new AuthToken();
        row.setToken(token);
        row.setUserId(userId);
        row.setExpireTime(LocalDateTime.now().plusDays(DAYS_VALID));
        repo.save(row);
        return token;
    }

    public Long resolve(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return repo.findByToken(token.trim())
                .filter(t -> t.getExpireTime() == null || t.getExpireTime().isAfter(LocalDateTime.now()))
                .map(AuthToken::getUserId)
                .orElse(null);
    }

    @Transactional
    public void remove(String token) {
        if (token != null && !token.isBlank()) {
            repo.deleteByToken(token.trim());
        }
    }
}
