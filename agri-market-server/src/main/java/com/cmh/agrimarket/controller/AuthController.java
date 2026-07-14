package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.dto.LoginRequest;
import com.cmh.agrimarket.dto.LoginResponse;
import com.cmh.agrimarket.dto.RegisterRequest;
import com.cmh.agrimarket.dto.UserVO;
import com.cmh.agrimarket.entity.User;
import com.cmh.agrimarket.repository.UserRepository;
import com.cmh.agrimarket.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @GetMapping("/me")
    public ApiResponse<UserVO> me() {
        CurrentUser current = CurrentUserHolder.require();
        User user = userRepository.findById(current.id()).orElseThrow();
        return ApiResponse.ok(UserVO.of(user));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-Token", required = false) String xToken) {
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7).trim();
        } else if (xToken != null && !xToken.isBlank()) {
            token = xToken.trim();
        }
        if (token != null && !token.isBlank()) {
            authService.logout(token);
        }
        return ApiResponse.ok();
    }
}
