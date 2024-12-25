package com.example.vitaqueue.orderProduct.domain.entity;

import com.example.vitaqueue.order.domain.entity.OrderEntity;
import com.example.vitaqueue.order.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Entity
@Table(name = "order_products")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_id")
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(nullable = false)
    private Long productId;

    @Column(name = "ordered_product_quantity", nullable = false)
    private int quantity;

    @Column(name = "ordered_product_price", nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus status;

    @Column(name = "registered_at")
    private Timestamp registeredAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @PrePersist
    void registeredAT() {
        this.registeredAt = Timestamp.from(Instant.now());
    }

    @PreUpdate
    void updatedAt() {
        this.updatedAt = Timestamp.from(Instant.now());
    }

    @Builder
    public OrderProductEntity(Long id, Long userId, OrderEntity order, Long productId, int quantity, BigDecimal price, OrderStatus status) {
        this.id = id;
        this.userId = userId;
        this.order = order;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.status = status;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

}
