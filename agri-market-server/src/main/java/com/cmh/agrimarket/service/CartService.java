package com.cmh.agrimarket.service;

import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.entity.CartItem;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.repository.CartItemRepository;
import com.cmh.agrimarket.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository repo;
    private final ProductRepository productRepo;

    public List<CartItem> list(Long userId) {
        return repo.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public long count(Long userId) {
        return repo.countByUserId(userId);
    }

    @Transactional
    public CartItem add(Long userId, Long productId, Integer quantity) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("商品不存在: " + productId));
        if (product.getStatus() != 1) {
            throw new IllegalStateException("商品已下架");
        }

        CartItem item = repo.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setUserId(userId);
                    newItem.setProduct(product);
                    return newItem;
                });

        int newQuantity = item.getQuantity() + (quantity != null ? quantity : 1);
        if (newQuantity > product.getStock()) {
            throw new IllegalStateException("库存不足");
        }
        item.setQuantity(newQuantity);
        return repo.save(item);
    }

    @Transactional
    public CartItem updateQuantity(Long userId, Long itemId, Integer quantity) {
        CartItem item = repo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("购物车项不存在: " + itemId));
        if (!userId.equals(item.getUserId())) {
            throw new IllegalStateException("无权操作该购物车项");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("数量不能小于1");
        }
        if (quantity > item.getProduct().getStock()) {
            throw new IllegalStateException("库存不足");
        }
        item.setQuantity(quantity);
        return repo.save(item);
    }

    @Transactional
    public void remove(Long userId, Long itemId) {
        CartItem item = repo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("购物车项不存在: " + itemId));
        if (!userId.equals(item.getUserId())) {
            throw new IllegalStateException("无权操作该购物车项");
        }
        repo.deleteById(itemId);
    }

    @Transactional
    public void clear(Long userId) {
        repo.deleteByUserId(userId);
    }
}