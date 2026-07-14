package com.cmh.agrimarket.service;

import com.cmh.agrimarket.common.AuthException;
import com.cmh.agrimarket.dto.LoginRequest;
import com.cmh.agrimarket.dto.LoginResponse;
import com.cmh.agrimarket.dto.RegisterRequest;
import com.cmh.agrimarket.dto.UserVO;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.entity.User;
import com.cmh.agrimarket.repository.UserRepository;
import com.cmh.agrimarket.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;

    public LoginResponse login(LoginRequest req) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
            SecurityUser principal = (SecurityUser) authentication.getPrincipal();
            User user = principal.getUser();
            String token = tokenService.issue(user.getId());
            return new LoginResponse(token, UserVO.of(user));
        } catch (AuthenticationException e) {
            throw new AuthException(401, "用户名或密码错误");
        }
    }

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new AuthException(400, "用户名已存在");
        }
        User user = new User();
        Role role = parseRegisterRole(req.role());
        user.setUsername(req.username().trim());
        user.setPassword(encoder.encode(req.password()));
        user.setNickname(req.nickname() == null || req.nickname().isBlank() ? req.username().trim() : req.nickname().trim());
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);
        String token = tokenService.issue(user.getId());
        return new LoginResponse(token, UserVO.of(user));
    }

    public void logout(String token) {
        tokenService.remove(token);
    }

    /** 供 DataInitializer 使用：按 BCrypt 写入原始密码。 */
    @Transactional
    public void ensureUser(String username, String rawPassword, String nickname, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(rawPassword));
        user.setNickname(nickname);
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);
    }

    private Role parseRegisterRole(String value) {
        if (value == null || value.isBlank() || "consumer".equalsIgnoreCase(value)) {
            return Role.CONSUMER;
        }
        if ("farmer".equalsIgnoreCase(value)) {
            return Role.FARMER;
        }
        throw new AuthException(400, "仅支持注册消费者或农户账号");
    }
}
