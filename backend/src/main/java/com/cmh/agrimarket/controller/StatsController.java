package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.dto.CategorySales;
import com.cmh.agrimarket.dto.StatsOverview;
import com.cmh.agrimarket.dto.TopProduct;
import com.cmh.agrimarket.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService service;

    @GetMapping("/overview")
    public ApiResponse<StatsOverview> overview() {
        return ApiResponse.ok(service.overview());
    }

    @GetMapping("/by-category")
    public ApiResponse<List<CategorySales>> byCategory() {
        return ApiResponse.ok(service.salesByCategory());
    }

    @GetMapping("/top-products")
    public ApiResponse<List<TopProduct>> topProducts(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok(service.topProducts(limit));
    }
}
