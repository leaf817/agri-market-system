package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.common.RequireRole;
import com.cmh.agrimarket.dto.UserAdminVO;
import com.cmh.agrimarket.dto.UserCreateRequest;
import com.cmh.agrimarket.dto.UserResetPasswordRequest;
import com.cmh.agrimarket.dto.UserUpdateRequest;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequireRole(Role.ADMIN)
@RequiredArgsConstructor
public class UserAdminController {
    private final UserAdminService userAdminService;

    @GetMapping
    public ApiResponse<List<UserAdminVO>> list() {
        return ApiResponse.ok(userAdminService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserAdminVO> get(@PathVariable Long id) {
        return ApiResponse.ok(userAdminService.get(id));
    }

    @PostMapping
    public ApiResponse<UserAdminVO> create(@Valid @RequestBody UserCreateRequest req) {
        return ApiResponse.ok(userAdminService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserAdminVO> update(@PathVariable Long id, @RequestBody UserUpdateRequest req) {
        return ApiResponse.ok(userAdminService.update(id, req, CurrentUserHolder.require().id()));
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody UserResetPasswordRequest req) {
        userAdminService.resetPassword(id, req);
        return ApiResponse.ok();
    }
}
