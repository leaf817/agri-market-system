package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.common.RequireRole;
import com.cmh.agrimarket.entity.Origin;
import com.cmh.agrimarket.entity.Role;
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
    public ApiResponse<List<Origin>> list(@RequestParam(required = false, defaultValue = "public") String scope) {
        return ApiResponse.ok(service.list(scope, CurrentUserHolder.require()));
    }

    @RequireRole({Role.ADMIN, Role.FARMER})
    @PostMapping
    public ApiResponse<Origin> save(@RequestBody Origin origin) {
        return ApiResponse.ok(service.save(origin, CurrentUserHolder.require()));
    }

    @RequireRole({Role.ADMIN, Role.FARMER})
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id, CurrentUserHolder.require());
        return ApiResponse.ok();
    }
}
