package com.cmh.agrimarket.service;

import com.cmh.agrimarket.common.AuthException;
import com.cmh.agrimarket.dto.UserAdminVO;
import com.cmh.agrimarket.dto.UserCreateRequest;
import com.cmh.agrimarket.dto.UserResetPasswordRequest;
import com.cmh.agrimarket.dto.UserUpdateRequest;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.entity.User;
import com.cmh.agrimarket.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserAdminVO> list() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(UserAdminVO::of)
                .toList();
    }

    public UserAdminVO get(Long id) {
        return UserAdminVO.of(find(id));
    }

    @Transactional
    public UserAdminVO create(UserCreateRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new AuthException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(req.username().trim());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setNickname(blankToNull(req.nickname()) == null ? req.username().trim() : req.nickname().trim());
        user.setPhone(blankToNull(req.phone()));
        user.setRole(parseRole(req.role()));
        user.setEnabled(req.enabled() == null || req.enabled());
        return UserAdminVO.of(userRepository.save(user));
    }

    @Transactional
    public UserAdminVO update(Long id, UserUpdateRequest req, Long operatorId) {
        User user = find(id);
        if (req.nickname() != null) user.setNickname(blankToNull(req.nickname()));
        if (req.phone() != null) user.setPhone(blankToNull(req.phone()));
        if (req.avatar() != null) user.setAvatar(blankToNull(req.avatar()));
        if (req.address() != null) user.setAddress(blankToNull(req.address()));
        if (req.role() != null) {
            Role newRole = parseRole(req.role());
            if (id.equals(operatorId) && newRole != Role.ADMIN) {
                throw new AuthException(400, "不能降低当前登录账号的管理员权限");
            }
            user.setRole(newRole);
        }
        if (req.enabled() != null) {
            if (id.equals(operatorId) && !req.enabled()) {
                throw new AuthException(400, "不能禁用当前登录账号");
            }
            user.setEnabled(req.enabled());
        }
        return UserAdminVO.of(userRepository.save(user));
    }

    @Transactional
    public void resetPassword(Long id, UserResetPasswordRequest req) {
        User user = find(id);
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + id));
    }

    private Role parseRole(String value) {
        if (value == null || value.isBlank()) {
            throw new AuthException(400, "角色不能为空");
        }
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AuthException(400, "角色不合法");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
