package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.common.CurrentUserHolder;
import com.cmh.agrimarket.entity.Address;
import com.cmh.agrimarket.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService service;

    @GetMapping
    public ApiResponse<List<Address>> list() {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.list(me.id()));
    }

    @GetMapping("/default")
    public ApiResponse<Address> getDefault() {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.getDefault(me.id()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Address> get(@PathVariable Long id) {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.get(me.id(), id));
    }

    @PostMapping
    public ApiResponse<Address> create(@RequestParam String name,
                                       @RequestParam String phone,
                                       @RequestParam String address,
                                       @RequestParam(defaultValue = "false") Boolean isDefault) {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.create(me.id(), name, phone, address, isDefault));
    }

    @PutMapping("/{id}")
    public ApiResponse<Address> update(@PathVariable Long id,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) String phone,
                                       @RequestParam(required = false) String address,
                                       @RequestParam(required = false) Boolean isDefault) {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.update(me.id(), id, name, phone, address, isDefault));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        CurrentUser me = CurrentUserHolder.require();
        service.delete(me.id(), id);
        return ApiResponse.ok();
    }

    @PatchMapping("/{id}/default")
    public ApiResponse<Address> setDefault(@PathVariable Long id) {
        CurrentUser me = CurrentUserHolder.require();
        return ApiResponse.ok(service.setDefault(me.id(), id));
    }
}