package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.common.RequireRole;
import com.cmh.agrimarket.entity.Review;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService service;

    @GetMapping
    public ApiResponse<List<Review>> list(@RequestParam(required = false) Long productId) {
        CurrentUser me = CurrentUserHolder.require();
        List<Review> reviews;
        if (productId != null) {
            reviews = service.listByProduct(productId);
        } else {
            reviews = service.listByUser(me.id());
        }
        return ApiResponse.ok(reviews);
    }

    @GetMapping("/product/{productId}/stats")
    public ApiResponse<Map<String, Object>> stats(@PathVariable Long productId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("count", service.count(productId));
        stats.put("avgRating", service.avgRating(productId));
        return ApiResponse.ok(stats);
    }

    @RequireRole(Role.CONSUMER)
    @PostMapping
    public ApiResponse<Review> create(@RequestParam Long productId,
                                      @RequestParam Integer rating,
                                      @RequestParam(required = false) String content) {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.create(me.id(), productId, rating, content));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        CurrentUser me = CurrentUserHolder.require();
        service.delete(me.id(), id);
        return ApiResponse.ok();
    }
}