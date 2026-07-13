package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.dto.UpdateProfileRequest;
import com.cmh.agrimarket.dto.UserVO;
import com.cmh.agrimarket.entity.User;
import com.cmh.agrimarket.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<UserVO> get() {
        CurrentUser me = CurrentUserHolder.require();
        User user = userRepository.findById(me.id()).orElseThrow();
        return ApiResponse.ok(UserVO.of(user));
    }

    @PutMapping
    public ApiResponse<UserVO> update(@Valid @RequestBody UpdateProfileRequest req) {
        CurrentUser me = CurrentUserHolder.require();
        User user = userRepository.findById(me.id()).orElseThrow();
        if (req.nickname() != null) {
            user.setNickname(req.nickname());
        }
        if (req.phone() != null) {
            user.setPhone(req.phone());
        }
        if (req.avatar() != null) {
            user.setAvatar(req.avatar());
        }
        if (req.address() != null) {
            user.setAddress(req.address());
        }
        userRepository.save(user);
        return ApiResponse.ok(UserVO.of(user));
    }
}