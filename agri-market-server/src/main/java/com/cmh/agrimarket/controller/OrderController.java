package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.common.RequireRole;
import com.cmh.agrimarket.dto.CancelOrderRequest;
import com.cmh.agrimarket.dto.CreateOrderRequest;
import com.cmh.agrimarket.dto.ShipOrderRequest;
import com.cmh.agrimarket.dto.UpdateOrderRequest;
import com.cmh.agrimarket.entity.OrderEntity;
import com.cmh.agrimarket.entity.OrderStatus;
import com.cmh.agrimarket.entity.Role;
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
        return ApiResponse.ok(service.list(CurrentUserHolder.require()));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderEntity> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id, CurrentUserHolder.require()));
    }

    @PostMapping
    public ApiResponse<OrderEntity> create(@Valid @RequestBody CreateOrderRequest req) {
        return ApiResponse.ok(service.create(req, CurrentUserHolder.require()));
    }

    @RequireRole({Role.ADMIN, Role.FARMER})
    @PatchMapping("/{id}/status")
    public ApiResponse<OrderEntity> changeStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ApiResponse.ok(service.changeStatus(id, status, CurrentUserHolder.require()));
    }

    @RequireRole({Role.ADMIN, Role.FARMER})
    @PutMapping("/{id}")
    public ApiResponse<OrderEntity> update(@PathVariable Long id, @Valid @RequestBody UpdateOrderRequest req) {
        return ApiResponse.ok(service.update(id, req, CurrentUserHolder.require()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id, CurrentUserHolder.require());
        return ApiResponse.ok();
    }

    /** 消费者模拟支付 */
    @RequireRole(Role.CONSUMER)
    @PostMapping("/{id}/pay")
    public ApiResponse<OrderEntity> pay(@PathVariable Long id) {
        return ApiResponse.ok(service.pay(id, CurrentUserHolder.require()));
    }

    /** 消费者取消订单 */
    @RequireRole(Role.CONSUMER)
    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderEntity> cancel(@PathVariable Long id, @RequestBody(required = false) CancelOrderRequest req) {
        return ApiResponse.ok(service.cancelByConsumer(id, req == null ? null : req.reason(), CurrentUserHolder.require()));
    }

    /** 农户/管理员模拟发货 */
    @RequireRole({Role.ADMIN, Role.FARMER})
    @PostMapping("/{id}/ship")
    public ApiResponse<OrderEntity> ship(@PathVariable Long id, @RequestBody(required = false) ShipOrderRequest req) {
        return ApiResponse.ok(service.ship(id, req, CurrentUserHolder.require()));
    }

    /** 消费者确认收货 */
    @RequireRole(Role.CONSUMER)
    @PostMapping("/{id}/confirm")
    public ApiResponse<OrderEntity> confirm(@PathVariable Long id) {
        return ApiResponse.ok(service.confirm(id, CurrentUserHolder.require()));
    }

    /** 管理员/农户后台关闭订单 */
    @RequireRole({Role.ADMIN, Role.FARMER})
    @PostMapping("/{id}/close")
    public ApiResponse<OrderEntity> close(@PathVariable Long id, @RequestBody(required = false) CancelOrderRequest req) {
        return ApiResponse.ok(service.close(id, req == null ? null : req.reason(), CurrentUserHolder.require()));
    }
}
