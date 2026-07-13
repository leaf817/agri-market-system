package com.cmh.agrimarket.repository;

import com.cmh.agrimarket.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserIdOrderByCreateTimeDesc(Long userId);

    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    Optional<CartItem> findByIdAndUserId(Long id, Long userId);

    List<CartItem> findByUserIdAndIdIn(Long userId, Collection<Long> ids);

    void deleteByUserId(Long userId);
}
