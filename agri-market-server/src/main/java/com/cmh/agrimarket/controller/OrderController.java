package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.dto.CreateOrderRequest;
import com.cmh.agrimarket.entity.OrderEntity;
import com.cmh.agrimarket.entity.OrderStatus;
import com.cmh.agrimarket.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @GetMapping
    public ApiResponse<List<OrderEntity>> list() {
        return ApiResponse.ok(service.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderEntity> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping
    public ApiResponse<OrderEntity> create(@Valid @RequestBody CreateOrderRequest req) {
        return ApiResponse.ok(service.create(req));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<OrderEntity> changeStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ApiResponse.ok(service.changeStatus(id, status));
    }
}
