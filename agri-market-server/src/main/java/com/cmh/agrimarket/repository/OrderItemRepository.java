package com.cmh.agrimarket.repository;

import com.cmh.agrimarket.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** 查询包含指定任一产品的订单 id（去重），用于农户查看「含本人产品的订单」。 */
    @Query("select distinct oi.order.id from OrderItem oi where oi.product.id in :productIds")
    List<Long> findDistinctOrderIdByProductIdIn(Collection<Long> productIds);
}
