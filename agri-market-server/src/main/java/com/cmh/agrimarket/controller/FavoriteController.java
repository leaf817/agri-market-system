package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.entity.Favorite;
import com.cmh.agrimarket.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService service;

    @GetMapping
    public ApiResponse<List<Favorite>> list() {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.list(me.id()));
    }

    @GetMapping("/count")
    public ApiResponse<Long> count() {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.count(me.id()));
    }

    @GetMapping("/check")
    public ApiResponse<Boolean> check(@RequestParam Long productId) {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.isFavorite(me.id(), productId));
    }

    @PostMapping
    public ApiResponse<Favorite> toggle(@RequestParam Long productId) {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.toggle(me.id(), productId));
    }
}