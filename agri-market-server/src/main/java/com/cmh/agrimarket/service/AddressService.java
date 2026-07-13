package com.cmh.agrimarket.service;

import com.cmh.agrimarket.entity.Address;
import com.cmh.agrimarket.repository.AddressRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository repo;

    public List<Address> list(Long userId) {
        return repo.findByUserIdOrderByIsDefaultDescCreateTimeDesc(userId);
    }

    public Address get(Long userId, Long id) {
        return repo.findByUserIdAndId(userId, id)
                .orElseThrow(() -> new EntityNotFoundException("地址不存在: " + id));
    }

    public Address getDefault(Long userId) {
        return repo.findByUserIdAndIsDefaultTrue(userId).orElse(null);
    }

    @Transactional
    public Address create(Long userId, String name, String phone, String address, Boolean isDefault) {
        Address addr = new Address();
        addr.setUserId(userId);
        addr.setName(name);
        addr.setPhone(phone);
        addr.setAddress(address);
        addr.setIsDefault(isDefault != null && isDefault);
        
        if (addr.getIsDefault()) {
            clearDefault(userId);
        }
        return repo.save(addr);
    }

    @Transactional
    public Address update(Long userId, Long id, String name, String phone, String address, Boolean isDefault) {
        Address addr = get(userId, id);
        if (name != null) addr.setName(name);
        if (phone != null) addr.setPhone(phone);
        if (address != null) addr.setAddress(address);
        if (isDefault != null) {
            if (isDefault && !addr.getIsDefault()) {
                clearDefault(userId);
            }
            addr.setIsDefault(isDefault);
        }
        return repo.save(addr);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Address addr = get(userId, id);
        repo.deleteById(id);
    }

    @Transactional
    public Address setDefault(Long userId, Long id) {
        Address addr = get(userId, id);
        if (!addr.getIsDefault()) {
            clearDefault(userId);
            addr.setIsDefault(true);
            repo.save(addr);
        }
        return addr;
    }

    private void clearDefault(Long userId) {
        repo.findByUserIdAndIsDefaultTrue(userId).ifPresent(addr -> {
            addr.setIsDefault(false);
            repo.save(addr);
        });
    }
}