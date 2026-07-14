package com.cmh.agrimarket.service;

import com.cmh.agrimarket.common.AuthException;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.dto.ShipOrderRequest;
import com.cmh.agrimarket.dto.CreateOrderRequest;
import com.cmh.agrimarket.dto.OrderItemRequest;
import com.cmh.agrimarket.dto.UpdateOrderRequest;
import com.cmh.agrimarket.entity.OrderEntity;
import com.cmh.agrimarket.entity.OrderItem;
import com.cmh.agrimarket.entity.OrderStatus;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.repository.OrderItemRepository;
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
    private final OrderItemRepository orderItemRepo;

    public List<OrderEntity> list(CurrentUser current) {
        if (current.role() == Role.ADMIN) {
            return orderRepo.findAllWithItems();
        }
        if (current.role() == Role.CONSUMER) {
            return orderRepo.findByCustomerId(current.id());
        }
        // FARMER：仅查看包含本人产品的订单
        List<Long> myProductIds = productRepo.findByFarmerId(current.id()).stream()
                .map(Product::getId).toList();
        if (myProductIds.isEmpty()) {
            return List.of();
        }
        List<Long> orderIds = orderItemRepo.findDistinctOrderIdByProductIdIn(myProductIds);
        if (orderIds.isEmpty()) {
            return List.of();
        }
        return orderRepo.findByIdIn(orderIds);
    }

    public OrderEntity get(Long id, CurrentUser current) {
        OrderEntity order = orderRepo.findWithItemsById(id)
                .orElseThrow(() -> new EntityNotFoundException("订单不存在: " + id));
        checkAccess(order, current);
        return order;
    }

    @Transactional
    public OrderEntity create(CreateOrderRequest req, CurrentUser current) {
        OrderEntity order = new OrderEntity();
        order.setOrderNo(generateOrderNo());
        order.setBuyerName(req.buyerName());
        order.setBuyerPhone(req.buyerPhone());
        order.setBuyerAddress(req.buyerAddress());
        order.setRemark(req.remark());
        order.setStatus(OrderStatus.PENDING);
        if (current != null) {
            order.setCustomerId(current.id());
        }

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
            // 扣库存（销量在支付/进入有效状态后再累计，避免待付款虚高）
            product.setStock(product.getStock() - qty);
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
    public OrderEntity changeStatus(Long id, OrderStatus newStatus, CurrentUser current) {
        OrderEntity order = get(id, current);
        applyStrictStatusChange(order, newStatus, current);
        return orderRepo.save(order);
    }

    /** 管理端编辑收货信息与状态 */
    @Transactional
    public OrderEntity update(Long id, UpdateOrderRequest req, CurrentUser current) {
        if (current.role() != Role.ADMIN && current.role() != Role.FARMER) {
            throw new AuthException(403, "仅管理员或农户可编辑订单");
        }
        OrderEntity order = get(id, current);
        order.setBuyerName(req.buyerName());
        order.setBuyerPhone(req.buyerPhone());
        order.setBuyerAddress(req.buyerAddress());
        order.setRemark(req.remark());
        if (req.status() != null && req.status() != order.getStatus()) {
            applyStrictStatusChange(order, req.status(), current);
        }
        return orderRepo.save(order);
    }

    /** 删除订单：仅待付款（回滚库存）或已取消可删；已支付/发货/完成禁止删以免库存错乱 */
    @Transactional
    public void delete(Long id, CurrentUser current) {
        OrderEntity order = get(id, current);
        if (current.role() == Role.CONSUMER) {
            if (!current.id().equals(order.getCustomerId())) {
                throw new AuthException(403, "只能删除自己的订单");
            }
        } else if (current.role() != Role.ADMIN && current.role() != Role.FARMER) {
            throw new AuthException(403, "无权删除订单");
        }
        OrderStatus status = order.getStatus();
        if (status == OrderStatus.PENDING) {
            restoreStock(order);
        } else if (status != OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("仅待付款或已取消的订单可以删除");
        }
        orderRepo.delete(order);
    }

    private void applyStrictStatusChange(OrderEntity order, OrderStatus newStatus, CurrentUser current) {
        OrderStatus old = order.getStatus();
        if (old == newStatus) {
            return;
        }
        if (old == OrderStatus.PENDING && newStatus == OrderStatus.PAID) {
            markPaid(order);
            return;
        }
        if (old == OrderStatus.PAID && newStatus == OrderStatus.SHIPPED) {
            if (current.role() != Role.ADMIN && current.role() != Role.FARMER) {
                throw new AuthException(403, "仅管理员或农户可发货");
            }
            markShipped(order, null);
            return;
        }
        if (old == OrderStatus.SHIPPED && newStatus == OrderStatus.COMPLETED) {
            if (current.role() != Role.CONSUMER) {
                throw new AuthException(403, "仅消费者可确认收货");
            }
            markCompleted(order);
            return;
        }
        if (newStatus == OrderStatus.CANCELLED) {
            if (old != OrderStatus.PENDING && old != OrderStatus.PAID) {
                throw new IllegalArgumentException("仅待付款或待发货订单可以取消");
            }
            cancelOrder(order, "后台关闭订单");
            return;
        }
        throw new IllegalArgumentException("不允许从 " + old.getLabel() + " 变更为 " + newStatus.getLabel());
    }

    private void restoreStock(OrderEntity order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product == null) {
                throw new IllegalStateException("订单商品已不存在，无法回滚库存，请先处理关联数据");
            }
            product.setStock(product.getStock() + item.getQuantity());
            productRepo.save(product);
        }
    }

    private void addSales(OrderEntity order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product == null) continue;
            int sales = product.getSales() == null ? 0 : product.getSales();
            product.setSales(sales + item.getQuantity());
            productRepo.save(product);
        }
    }

    private void subtractSales(OrderEntity order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product == null) continue;
            int sales = product.getSales() == null ? 0 : product.getSales();
            product.setSales(Math.max(0, sales - item.getQuantity()));
            productRepo.save(product);
        }
    }

    /** 消费者模拟支付：待付款 → 待发货 */
    @Transactional
    public OrderEntity pay(Long id, CurrentUser current) {
        if (current.role() != Role.CONSUMER) {
            throw new AuthException(403, "仅消费者可支付订单");
        }
        OrderEntity order = get(id, current);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("只有待付款订单可以支付");
        }
        markPaid(order);
        return orderRepo.save(order);
    }

    /** 消费者取消订单：待付款可取消；待发货作为简化流程也允许取消并回滚销量/库存。 */
    @Transactional
    public OrderEntity cancelByConsumer(Long id, String reason, CurrentUser current) {
        if (current.role() != Role.CONSUMER) {
            throw new AuthException(403, "仅消费者可取消本人订单");
        }
        OrderEntity order = get(id, current);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("仅待付款或待发货订单可以取消");
        }
        cancelOrder(order, reason == null || reason.isBlank() ? "消费者取消订单" : reason);
        return orderRepo.save(order);
    }

    /** 农户/管理员模拟发货：待发货 → 已发货 */
    @Transactional
    public OrderEntity ship(Long id, ShipOrderRequest req, CurrentUser current) {
        if (current.role() != Role.ADMIN && current.role() != Role.FARMER) {
            throw new AuthException(403, "仅管理员或农户可发货");
        }
        OrderEntity order = get(id, current);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("只有待发货订单可以发货");
        }
        markShipped(order, req);
        return orderRepo.save(order);
    }

    /** 消费者确认收货：已发货 → 已完成 */
    @Transactional
    public OrderEntity confirm(Long id, CurrentUser current) {
        if (current.role() != Role.CONSUMER) {
            throw new AuthException(403, "仅消费者可确认收货");
        }
        OrderEntity order = get(id, current);
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("只有已发货订单可以确认收货");
        }
        markCompleted(order);
        return orderRepo.save(order);
    }

    /** 农户/管理员后台关闭订单：待付款/待发货可关闭。 */
    @Transactional
    public OrderEntity close(Long id, String reason, CurrentUser current) {
        if (current.role() != Role.ADMIN && current.role() != Role.FARMER) {
            throw new AuthException(403, "仅管理员或农户可关闭订单");
        }
        OrderEntity order = get(id, current);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("仅待付款或待发货订单可以关闭");
        }
        cancelOrder(order, reason == null || reason.isBlank() ? "后台关闭订单" : reason);
        return orderRepo.save(order);
    }

    private void markPaid(OrderEntity order) {
        addSales(order);
        order.setStatus(OrderStatus.PAID);
        order.setPayTime(LocalDateTime.now());
    }

    private void markShipped(OrderEntity order, ShipOrderRequest req) {
        order.setStatus(OrderStatus.SHIPPED);
        order.setShipTime(LocalDateTime.now());
        if (req != null) {
            order.setDeliveryCompany(blankToNull(req.deliveryCompany()));
            order.setTrackingNo(blankToNull(req.trackingNo()));
            order.setDeliveryRemark(blankToNull(req.deliveryRemark()));
        }
    }

    private void markCompleted(OrderEntity order) {
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompleteTime(LocalDateTime.now());
    }

    private void cancelOrder(OrderEntity order, String reason) {
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("当前订单状态不允许取消");
        }
        restoreStock(order);
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.SHIPPED) {
            subtractSales(order);
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** 访问校验：消费者仅看本人订单，农户仅看含本人产品的订单。 */
    private void checkAccess(OrderEntity order, CurrentUser current) {
        if (current == null) {
            throw new AuthException(401, "未登录或登录已过期");
        }
        if (current.role() == Role.CONSUMER && !current.id().equals(order.getCustomerId())) {
            throw new AuthException(403, "无权查看该订单");
        }
        if (current.role() == Role.FARMER) {
            boolean owns = order.getItems().stream()
                    .anyMatch(it -> it.getProduct() != null && current.id().equals(it.getProduct().getFarmerId()));
            if (!owns) {
                throw new AuthException(403, "该订单不包含您的农产品");
            }
        }
    }

    private String generateOrderNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "AM" + ts + rand;
    }
}
