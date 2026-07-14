package com.cmh.agrimarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String orderNo;

    @Column(nullable = false, length = 50)
    private String buyerName;

    @Column(length = 30)
    private String buyerPhone;

    @Column(length = 200)
    private String buyerAddress;

    /** 下单消费者用户 id */
    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(length = 500)
    private String remark;

    @Column(length = 80)
    private String deliveryCompany;

    @Column(length = 80)
    private String trackingNo;

    @Column(length = 500)
    private String deliveryRemark;

    @Column(length = 500)
    private String cancelReason;

    private LocalDateTime payTime;

    private LocalDateTime shipTime;

    private LocalDateTime completeTime;

    private LocalDateTime cancelTime;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }
}
