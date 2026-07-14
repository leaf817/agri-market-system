package com.cmh.agrimarket.common;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置 + 鉴权拦截器注册。
 * 允许前端 Vite 开发服务器（默认 5173）直连后端；前端 dev proxy 已代理 /api。
 * 拦截器保护 /api/**；仅放行登录/注册/退出（/me 仍需鉴权解析 token）。
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/logout"
                );
    }
}
