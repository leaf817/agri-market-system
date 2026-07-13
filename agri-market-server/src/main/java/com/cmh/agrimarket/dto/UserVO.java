package com.cmh.agrimarket.dto;

import com.cmh.agrimarket.entity.User;

/**
 * 返回给前端的用户信息（不含密码）。role 转小写，便于前端 v-if 判断。
 */
public record UserVO(
        Long id,
        String username,
        String nickname,
        String phone,
        String avatar,
        String address,
        String role
) {
    public static UserVO of(User u) {
        return new UserVO(u.getId(), u.getUsername(), u.getNickname(),
                u.getPhone(), u.getAvatar(), u.getAddress(),
                u.getRole() == null ? null : u.getRole().name().toLowerCase());
    }
}
