package com.cmh.agrimarket.service;

import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.entity.Favorite;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.repository.FavoriteRepository;
import com.cmh.agrimarket.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository repo;
    private final ProductRepository productRepo;

    public List<Favorite> list(Long userId) {
        return repo.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public boolean isFavorite(Long userId, Long productId) {
        return repo.existsByUserIdAndProductId(userId, productId);
    }

    public long count(Long userId) {
        return repo.countByUserId(userId);
    }

    @Transactional
    public Favorite toggle(Long userId, Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("商品不存在: " + productId));

        if (repo.existsByUserIdAndProductId(userId, productId)) {
            repo.deleteByUserIdAndProductId(userId, productId);
            return null;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProduct(product);
        return repo.save(favorite);
    }
}