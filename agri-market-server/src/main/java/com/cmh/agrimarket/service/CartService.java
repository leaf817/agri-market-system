package com.cmh.agrimarket.service;

import com.cmh.agrimarket.common.AuthException;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.dto.CartAddRequest;
import com.cmh.agrimarket.dto.CartCheckoutRequest;
import com.cmh.agrimarket.dto.CartUpdateRequest;
import com.cmh.agrimarket.dto.CreateOrderRequest;
import com.cmh.agrimarket.dto.OrderItemRequest;
import com.cmh.agrimarket.entity.CartItem;
import com.cmh.agrimarket.entity.OrderEntity;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.entity.Role;
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
    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;
    private final OrderService orderService;

    public List<CartItem> list(CurrentUser current) {
        requireConsumer(current);
        return cartRepo.findByUserIdOrderByCreateTimeDesc(current.id());
    }

    @Transactional
    public CartItem add(CartAddRequest req, CurrentUser current) {
        requireConsumer(current);
        Product product = productRepo.findById(req.productId())
                .orElseThrow(() -> new EntityNotFoundException("商品不存在: " + req.productId()));
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new IllegalArgumentException("商品已下架，无法加入购物车");
        }
        if (product.getStock() == null || product.getStock() <= 0) {
            throw new IllegalArgumentException("商品暂无库存");
        }

        CartItem item = cartRepo.findByUserIdAndProductId(current.id(), product.getId()).orElse(null);
        int qty = req.quantity();
        if (item == null) {
            item = new CartItem();
            item.setUserId(current.id());
            item.setProduct(product);
            item.setQuantity(qty);
        } else {
            item.setQuantity(item.getQuantity() + qty);
        }
        if (item.getQuantity() > product.getStock()) {
            throw new IllegalArgumentException("购物车数量不能超过库存（当前库存 " + product.getStock() + "）");
        }
        return cartRepo.save(item);
    }

    @Transactional
    public CartItem update(Long id, CartUpdateRequest req, CurrentUser current) {
        requireConsumer(current);
        CartItem item = cartRepo.findByIdAndUserId(id, current.id())
                .orElseThrow(() -> new EntityNotFoundException("购物车条目不存在"));
        Product product = item.getProduct();
        if (product == null) {
            throw new IllegalArgumentException("商品已失效，请移除后重试");
        }
        if (req.quantity() > product.getStock()) {
            throw new IllegalArgumentException("数量不能超过库存（当前库存 " + product.getStock() + "）");
        }
        item.setQuantity(req.quantity());
        return cartRepo.save(item);
    }

    @Transactional
    public void remove(Long id, CurrentUser current) {
        requireConsumer(current);
        CartItem item = cartRepo.findByIdAndUserId(id, current.id())
                .orElseThrow(() -> new EntityNotFoundException("购物车条目不存在"));
        cartRepo.delete(item);
    }

    @Transactional
    public void clear(CurrentUser current) {
        requireConsumer(current);
        cartRepo.deleteByUserId(current.id());
    }

    /** 勾选结算：创建订单后移除已结算的购物车条目。 */
    @Transactional
    public OrderEntity checkout(CartCheckoutRequest req, CurrentUser current) {
        requireConsumer(current);
        List<CartItem> items = cartRepo.findByUserIdAndIdIn(current.id(), req.cartItemIds());
        if (items.isEmpty()) {
            throw new IllegalArgumentException("未找到可结算的购物车商品");
        }
        List<OrderItemRequest> orderItems = items.stream()
                .map(it -> new OrderItemRequest(it.getProduct().getId(), it.getQuantity()))
                .toList();
        OrderEntity order = orderService.create(new CreateOrderRequest(
                req.buyerName(), req.buyerPhone(), req.buyerAddress(), req.remark(), orderItems
        ), current);
        cartRepo.deleteAll(items);
        return order;
    }

    private void requireConsumer(CurrentUser current) {
        if (current == null) {
            throw new AuthException(401, "未登录或登录已过期");
        }
        if (current.role() != Role.CONSUMER) {
            throw new AuthException(403, "仅消费者可使用购物车");
        }
    }
}
