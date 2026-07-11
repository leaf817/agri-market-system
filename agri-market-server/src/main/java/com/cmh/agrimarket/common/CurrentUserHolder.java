package com.cmh.agrimarket.common;

/**
 * 用 ThreadLocal 在一次请求内传递当前用户。拦截器 preHandle 写入、afterCompletion 清理。
 */
public final class CurrentUserHolder {
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private CurrentUserHolder() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static CurrentUser require() {
        CurrentUser u = HOLDER.get();
        if (u == null) {
            throw new AuthException(401, "未登录或登录已过期");
        }
        return u;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
