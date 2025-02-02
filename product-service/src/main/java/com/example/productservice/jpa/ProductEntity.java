package com.example.productservice.jpa;


import com.example.productservice.dto.request.ProductRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product")
@SQLDelete(sql = "UPDATE product SET deleted_at = NOW() where id=?")
@SQLRestriction("deleted_at is NULL")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "descript", nullable = false, length = 1000)
    private String descript;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "is_flash_sale", nullable = false)
    private boolean isFlashSale;

    @Column(name = "is_flash_sale_open", nullable = false)
    private boolean isFlashSaleOpen;

    @Column(name = "start_at")
    private Timestamp startAt;

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

    public ProductEntity of(ProductRequest request, Long userId) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(request.getName());
        productEntity.setUserId(userId);
        productEntity.setPrice(request.getPrice());
        productEntity.setDescript(request.getDescript());
        productEntity.setCategory(request.getCategory());
        productEntity.setFlashSale(request.isFlashSale());

        return productEntity;
    }

    public void updateFlashSaleOpen(boolean eventOpen) {
        this.isFlashSaleOpen = true;
    }
}
