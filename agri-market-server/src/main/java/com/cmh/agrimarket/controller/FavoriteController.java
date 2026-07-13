package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.common.RequireRole;
import com.cmh.agrimarket.entity.Favorite;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@RequireRole(Role.CONSUMER)
public class FavoriteController {
    private final FavoriteService service;

    @GetMapping
    public ApiResponse<List<Favorite>> list() {
        return ApiResponse.ok(service.list(CurrentUserHolder.require()));
    }

    @GetMapping("/ids")
    public ApiResponse<List<Long>> ids() {
        return ApiResponse.ok(service.productIds(CurrentUserHolder.require()));
    }

    @PostMapping("/{productId}")
    public ApiResponse<Favorite> add(@PathVariable Long productId) {
        return ApiResponse.ok(service.add(productId, CurrentUserHolder.require()));
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> remove(@PathVariable Long productId) {
        service.remove(productId, CurrentUserHolder.require());
        return ApiResponse.ok();
    }

    @PostMapping("/toggle/{productId}")
    public ApiResponse<Map<String, Object>> toggle(@PathVariable Long productId) {
        return ApiResponse.ok(service.toggle(productId, CurrentUserHolder.require()));
    }
}
