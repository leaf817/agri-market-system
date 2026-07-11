package com.cmh.agrimarket.service;

import com.cmh.agrimarket.dto.CreateOrderRequest;
import com.cmh.agrimarket.dto.OrderItemRequest;
import com.cmh.agrimarket.entity.OrderEntity;
import com.cmh.agrimarket.entity.OrderItem;
import com.cmh.agrimarket.entity.OrderStatus;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.repository.OrderRepository;
import com.cmh.agrimarket.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;

    public List<OrderEntity> list() {
        return orderRepo.findAllWithItems();
    }

    public OrderEntity get(Long id) {
        return orderRepo.findWithItemsById(id)
                .orElseThrow(() -> new EntityNotFoundException("订单不存在: " + id));
    }

    @Transactional
    public OrderEntity create(CreateOrderRequest req) {
        OrderEntity order = new OrderEntity();
        order.setOrderNo(generateOrderNo());
        order.setBuyerName(req.buyerName());
        order.setBuyerPhone(req.buyerPhone());
        order.setBuyerAddress(req.buyerAddress());
        order.setRemark(req.remark());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : req.items()) {
            Product product = productRepo.findById(itemReq.productId())
                    .orElseThrow(() -> new EntityNotFoundException("商品不存在: " + itemReq.productId()));
            int qty = itemReq.quantity();
            if (product.getStatus() != 1) {
                throw new IllegalArgumentException("商品已下架: " + product.getName());
            }
            if (product.getStock() < qty) {
                throw new IllegalArgumentException("商品库存不足: " + product.getName());
            }
            // 扣库存、累加销量
            product.setStock(product.getStock() - qty);
            product.setSales(product.getSales() + qty);
            productRepo.save(product);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(qty);
            BigDecimal amount = product.getPrice().multiply(BigDecimal.valueOf(qty));
            item.setAmount(amount);
            order.addItem(item);
            total = total.add(amount);
        }
        order.setTotalAmount(total);
        return orderRepo.save(order);
    }

    @Transactional
    public OrderEntity changeStatus(Long id, OrderStatus newStatus) {
        OrderEntity order = get(id);
        OrderStatus old = order.getStatus();
        if (old == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("已取消的订单不能变更状态");
        }
        if (newStatus == OrderStatus.CANCELLED && old != OrderStatus.CANCELLED) {
            // 取消订单：回滚库存与销量
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(Math.max(0, product.getSales() - item.getQuantity()));
                productRepo.save(product);
            }
        }
        order.setStatus(newStatus);
        return orderRepo.save(order);
    }

    private String generateOrderNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "AM" + ts + rand;
    }
}
