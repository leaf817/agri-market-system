package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.common.RequireRole;
import com.cmh.agrimarket.dto.CartAddRequest;
import com.cmh.agrimarket.dto.CartCheckoutRequest;
import com.cmh.agrimarket.dto.CartUpdateRequest;
import com.cmh.agrimarket.entity.CartItem;
import com.cmh.agrimarket.entity.OrderEntity;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@RequireRole(Role.CONSUMER)
public class CartController {
    private final CartService service;

    @GetMapping
    public ApiResponse<List<CartItem>> list() {
        return ApiResponse.ok(service.list(CurrentUserHolder.require()));
    }

    @PostMapping
    public ApiResponse<CartItem> add(@Valid @RequestBody CartAddRequest req) {
        return ApiResponse.ok(service.add(req, CurrentUserHolder.require()));
    }

    @PutMapping("/{id}")
    public ApiResponse<CartItem> update(@PathVariable Long id, @Valid @RequestBody CartUpdateRequest req) {
        return ApiResponse.ok(service.update(id, req, CurrentUserHolder.require()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        service.remove(id, CurrentUserHolder.require());
        return ApiResponse.ok();
    }

    @DeleteMapping
    public ApiResponse<Void> clear() {
        service.clear(CurrentUserHolder.require());
        return ApiResponse.ok();
    }

    @PostMapping("/checkout")
    public ApiResponse<OrderEntity> checkout(@Valid @RequestBody CartCheckoutRequest req) {
        return ApiResponse.ok(service.checkout(req, CurrentUserHolder.require()));
    }
}
