package com.example.vitaqueue.order.domain.entity;

import com.example.vitaqueue.order.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 255)
    private String deliveryAddress;

    @Column(length = 15)
    private String receiverPhone;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Timestamp registeredAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Enumerated(EnumType.STRING) // Enum 값을 저장할 때 문자열로 저장
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @PrePersist
    protected void onCreate() {
        this.registeredAt = Timestamp.from(Instant.now());
        this.updatedAt = Timestamp.from(Instant.now());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Timestamp.from(Instant.now());
    }

    @Builder
    public OrderEntity(Long id, Long userId, BigDecimal totalPrice, String deliveryAddress,
                       String receiverPhone, OrderStatus status) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.deliveryAddress = deliveryAddress;
        this.receiverPhone = receiverPhone;
        this.status = status != null ? status : OrderStatus.CREATED;
    }

    public void updateTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }
}
