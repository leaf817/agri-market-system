package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.entity.Origin;
import com.cmh.agrimarket.service.OriginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/origins")
@RequiredArgsConstructor
public class OriginController {
    private final OriginService service;

    @GetMapping
    public ApiResponse<List<Origin>> list() {
        return ApiResponse.ok(service.list());
    }

    @PostMapping
    public ApiResponse<Origin> save(@RequestBody Origin origin) {
        return ApiResponse.ok(service.save(origin));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
