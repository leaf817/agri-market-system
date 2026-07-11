package com.cmh.agrimarket.service;

import com.cmh.agrimarket.common.AuthException;
import com.cmh.agrimarket.dto.LoginRequest;
import com.cmh.agrimarket.dto.LoginResponse;
import com.cmh.agrimarket.dto.RegisterRequest;
import com.cmh.agrimarket.dto.UserVO;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.entity.User;
import com.cmh.agrimarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new AuthException(401, "用户名或密码错误"));
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new AuthException(401, "账号已被禁用");
        }
        if (!encoder.matches(req.password(), user.getPassword())) {
            throw new AuthException(401, "用户名或密码错误");
        }
        String token = tokenService.issue(user.getId());
        return new LoginResponse(token, UserVO.of(user));
    }

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new AuthException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(encoder.encode(req.password()));
        user.setNickname(req.nickname() == null || req.nickname().isBlank() ? req.username() : req.nickname());
        user.setRole(Role.CONSUMER);
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
}
