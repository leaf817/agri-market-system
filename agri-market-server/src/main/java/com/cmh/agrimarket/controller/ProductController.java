package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.common.RequireRole;
import com.cmh.agrimarket.dto.ProductRequest;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;

    @GetMapping
    public ApiResponse<List<Product>> list(@RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false, defaultValue = "public") String scope) {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.listForScope(scope, categoryId, status, keyword, me));
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @RequireRole({Role.ADMIN, Role.FARMER})
    @PostMapping
    public ApiResponse<Product> create(@Valid @RequestBody ProductRequest req) {
        return ApiResponse.ok(service.create(req, CurrentUserHolder.require()));
    }

    @RequireRole({Role.ADMIN, Role.FARMER})
    @PutMapping("/{id}")
    public ApiResponse<Product> update(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
        return ApiResponse.ok(service.update(id, req, CurrentUserHolder.require()));
    }

    @RequireRole({Role.ADMIN, Role.FARMER})
    @PatchMapping("/{id}/status")
    public ApiResponse<Product> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        return ApiResponse.ok(service.changeStatus(id, status, CurrentUserHolder.require()));
    }

    @RequireRole({Role.ADMIN, Role.FARMER})
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id, CurrentUserHolder.require());
        return ApiResponse.ok();
    }
}
