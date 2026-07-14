package com.cmh.agrimarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 登录 Token 持久化：业务数据在 MySQL，重启后端不丢订单等；
 * Token 也落库，重新登录前仍可校验。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auth_token")
public class AuthToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "expire_time")
    private LocalDateTime expireTime;
}
