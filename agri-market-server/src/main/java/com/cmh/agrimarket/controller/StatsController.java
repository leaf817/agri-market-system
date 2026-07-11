package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.common.RequireRole;
import com.cmh.agrimarket.dto.CategorySales;
import com.cmh.agrimarket.dto.StatsOverview;
import com.cmh.agrimarket.dto.TopProduct;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@RequireRole({Role.ADMIN, Role.FARMER})
public class StatsController {
    private final StatsService service;

    private Long farmerScope() {
        CurrentUser me = CurrentUserHolder.require();
        return me.role() == Role.FARMER ? me.id() : null;
    }

    @GetMapping("/overview")
    public ApiResponse<StatsOverview> overview() {
        return ApiResponse.ok(service.overview(farmerScope()));
    }

    @GetMapping("/by-category")
    public ApiResponse<List<CategorySales>> byCategory() {
        return ApiResponse.ok(service.salesByCategory(farmerScope()));
    }

    @GetMapping("/top-products")
    public ApiResponse<List<TopProduct>> topProducts(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok(service.topProducts(limit, farmerScope()));
    }
}
