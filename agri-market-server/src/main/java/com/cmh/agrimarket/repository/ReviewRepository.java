package com.cmh.agrimarket.repository;

import com.cmh.agrimarket.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdOrderByCreateTimeDesc(Long productId);

    List<Review> findByUserIdOrderByCreateTimeDesc(Long userId);

    Optional<Review> findByOrderItemId(Long orderItemId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    long countByProductId(Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double avgRatingByProductId(@Param("productId") Long productId);
}