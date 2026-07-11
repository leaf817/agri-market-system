package com.cmh.agrimarket.common;

import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.entity.User;
import com.cmh.agrimarket.repository.UserRepository;
import com.cmh.agrimarket.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 鉴权拦截器：解析 Authorization Bearer token -> 当前用户，并按 @RequireRole 校验角色。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final TokenService tokenService;
    private final UserRepository userRepository;

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

        String token = extractToken(request);
        Long userId = tokenService.resolve(token);
        if (userId == null) {
            throw new AuthException(401, "未登录或登录已过期");
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || Boolean.FALSE.equals(user.getEnabled())) {
            throw new AuthException(401, "账号不存在或已被禁用");
        }
        CurrentUser current = new CurrentUser(user.getId(), user.getNickname(), user.getRole());
        CurrentUserHolder.set(current);

        RequireRole require = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (require == null) {
            require = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (require != null && require.value().length > 0) {
            boolean allowed = Arrays.stream(require.value()).anyMatch(r -> r == user.getRole());
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

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        String xToken = request.getHeader("X-Token");
        if (xToken != null && !xToken.isBlank()) {
            return xToken.trim();
        }
        return null;
    }
}
