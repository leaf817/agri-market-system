package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.entity.CartItem;
import com.cmh.agrimarket.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService service;

    @GetMapping
    public ApiResponse<List<CartItem>> list() {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.list(me.id()));
    }

    @GetMapping("/count")
    public ApiResponse<Long> count() {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.count(me.id()));
    }

    @PostMapping
    public ApiResponse<CartItem> add(@RequestParam Long productId,
                                     @RequestParam(defaultValue = "1") Integer quantity) {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.add(me.id(), productId, quantity));
    }

    @PutMapping("/{id}/quantity")
    public ApiResponse<CartItem> updateQuantity(@PathVariable Long id,
                                                @RequestParam Integer quantity) {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.updateQuantity(me.id(), id, quantity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        CurrentUser me = CurrentUserHolder.require();
        service.remove(me.id(), id);
        return ApiResponse.ok();
    }

    @DeleteMapping
    public ApiResponse<Void> clear() {
        CurrentUser me = CurrentUserHolder.require();
        service.clear(me.id());
        return ApiResponse.ok();
    }
}