package com.cmh.agrimarket.common;

import com.cmh.agrimarket.entity.Role;

/**
 * 当前登录用户的轻量快照，由拦截器写入 CurrentUserHolder。
 */
public record CurrentUser(Long id, String nickname, Role role) {
}
