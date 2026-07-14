package com.cmh.agrimarket.service;

import com.cmh.agrimarket.entity.OrderEntity;
import com.cmh.agrimarket.entity.OrderStatus;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.entity.Review;
import com.cmh.agrimarket.repository.OrderRepository;
import com.cmh.agrimarket.repository.ProductRepository;
import com.cmh.agrimarket.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository repo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    public List<Review> listByProduct(Long productId) {
        return repo.findByProductIdOrderByCreateTimeDesc(productId);
    }

    public List<Review> listByUser(Long userId) {
        return repo.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public long count(Long productId) {
        return repo.countByProductId(productId);
    }

    public Double avgRating(Long productId) {
        return repo.avgRatingByProductId(productId);
    }

    @Transactional
    public Review create(Long userId, Long productId, Integer rating, String content) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("商品不存在: " + productId));
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }
        if (repo.existsByUserIdAndProductId(userId, productId)) {
            throw new IllegalStateException("您已评价过该商品");
        }

        boolean hasCompletedOrder = orderRepo.findByCustomerId(userId).stream()
                .anyMatch(order -> order.getStatus() == OrderStatus.COMPLETED
                        && order.getItems().stream().anyMatch(item ->
                        item.getProduct() != null && productId.equals(item.getProduct().getId())));

        if (!hasCompletedOrder) {
            throw new IllegalStateException("只有完成订单后才能评价该商品");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setProduct(product);
        review.setRating(rating);
        review.setContent(content);
        return repo.save(review);
    }

    @Transactional
    public void delete(Long userId, Long reviewId) {
        Review review = repo.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("评价不存在: " + reviewId));
        if (!userId.equals(review.getUserId())) {
            throw new IllegalStateException("无权删除该评价");
        }
        repo.deleteById(reviewId);
    }
}