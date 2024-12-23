package com.example.vitaqueue.product.model.entity;

import com.example.vitaqueue.product.dto.request.ProductRequest;
import com.example.vitaqueue.user.model.entity.UserEntity;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userId;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "descript", nullable = false, length = 1000)
    private String descript;

    @Column(name = "category", nullable = false)
    private String category;

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

    public ProductEntity of(ProductRequest request, UserEntity userEntity) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(request.getName());
        productEntity.setUserId(userEntity);
        productEntity.setPrice(request.getPrice());
        productEntity.setDescript(request.getDescript());
        productEntity.setCategory(request.getCategory());

        return productEntity;
    }
}
