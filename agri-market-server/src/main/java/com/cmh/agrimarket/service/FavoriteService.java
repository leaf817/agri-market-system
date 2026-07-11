package com.cmh.agrimarket.service;

import com.cmh.agrimarket.common.AuthException;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.entity.Favorite;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.repository.FavoriteRepository;
import com.cmh.agrimarket.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepo;
    private final ProductRepository productRepo;

    public List<Favorite> list(CurrentUser current) {
        requireConsumer(current);
        return favoriteRepo.findByUserIdOrderByCreateTimeDesc(current.id());
    }

    public List<Long> productIds(CurrentUser current) {
        requireConsumer(current);
        return favoriteRepo.findProductIdsByUserId(current.id());
    }

    @Transactional
    public Favorite add(Long productId, CurrentUser current) {
        requireConsumer(current);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("商品不存在: " + productId));
        return favoriteRepo.findByUserIdAndProductId(current.id(), productId).orElseGet(() -> {
            Favorite f = new Favorite();
            f.setUserId(current.id());
            f.setProduct(product);
            return favoriteRepo.save(f);
        });
    }

    @Transactional
    public void remove(Long productId, CurrentUser current) {
        requireConsumer(current);
        favoriteRepo.deleteByUserIdAndProductId(current.id(), productId);
    }

    /** 切换收藏状态，返回当前是否已收藏。 */
    @Transactional
    public Map<String, Object> toggle(Long productId, CurrentUser current) {
        requireConsumer(current);
        if (favoriteRepo.existsByUserIdAndProductId(current.id(), productId)) {
            favoriteRepo.deleteByUserIdAndProductId(current.id(), productId);
            return Map.of("favorited", false);
        }
        add(productId, current);
        return Map.of("favorited", true);
    }

    private void requireConsumer(CurrentUser current) {
        if (current == null) {
            throw new AuthException(401, "未登录或登录已过期");
        }
        if (current.role() != Role.CONSUMER) {
            throw new AuthException(403, "仅消费者可使用收藏");
        }
    }
}
