package com.cmh.agrimarket.repository;

import com.cmh.agrimarket.entity.OrderEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.category", "items.product.origin"})
    @Query("select o from OrderEntity o order by o.createTime desc")
    List<OrderEntity> findAllWithItems();

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.category", "items.product.origin"})
    @Query("select o from OrderEntity o where o.id = :id")
    Optional<OrderEntity> findWithItemsById(Long id);
}
