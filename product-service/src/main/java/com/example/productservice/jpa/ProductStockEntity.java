package com.example.productservice.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "productStock")
public class ProductStockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "stock")
    private Integer stock;

    public ProductStockEntity of(Long productId, Integer stock) {
        ProductStockEntity productStockEntity = new ProductStockEntity();
        productStockEntity.setProductId(productId);
        productStockEntity.setStock(stock);
        return productStockEntity;
    }
}
