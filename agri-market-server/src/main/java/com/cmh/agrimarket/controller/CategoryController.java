package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.RequireRole;
import com.cmh.agrimarket.entity.Category;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @GetMapping
    public ApiResponse<List<Category>> list() {
        return ApiResponse.ok(service.list());
    }

    @RequireRole(Role.ADMIN)
    @PostMapping
    public ApiResponse<Category> save(@RequestBody Category category) {
        return ApiResponse.ok(service.save(category));
    }

    @RequireRole(Role.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
