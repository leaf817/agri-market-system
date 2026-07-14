package com.cmh.agrimarket.common;

import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.security.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 业务权限拦截器：基于 Spring Security 当前认证用户，按 @RequireRole 校验角色。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 非 Controller 方法（如静态资源）直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof SecurityUser principal)) {
            throw new AuthException(401, "未登录或登录已过期");
        }
        CurrentUser current = new CurrentUser(principal.getId(), principal.getNickname(), principal.getRole());
        CurrentUserHolder.set(current);

        RequireRole require = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (require == null) {
            require = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (require != null && require.value().length > 0) {
            boolean allowed = Arrays.stream(require.value()).anyMatch(r -> r == principal.getRole());
            if (!allowed) {
                throw new AuthException(403, "没有访问权限");
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserHolder.clear();
    }
}
