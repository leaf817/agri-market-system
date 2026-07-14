package com.cmh.agrimarket.dto;

import com.cmh.agrimarket.entity.User;

import java.time.LocalDateTime;

public record UserAdminVO(
        Long id,
        String username,
        String nickname,
        String phone,
        String avatar,
        String address,
        String role,
        Boolean enabled,
        LocalDateTime createTime
) {
    public static UserAdminVO of(User u) {
        return new UserAdminVO(
                u.getId(),
                u.getUsername(),
                u.getNickname(),
                u.getPhone(),
                u.getAvatar(),
                u.getAddress(),
                u.getRole() == null ? null : u.getRole().name().toLowerCase(),
                u.getEnabled(),
                u.getCreateTime()
        );
    }
}
