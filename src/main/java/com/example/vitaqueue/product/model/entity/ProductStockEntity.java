package com.example.vitaqueue.product.model.entity;

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

    @OneToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @Column(name = "stock")
    private Long stock;

    public ProductStockEntity of(ProductEntity product, Long stock) {
        ProductStockEntity productStockEntity = new ProductStockEntity();
        productStockEntity.setProduct(product);
        productStockEntity.setStock(stock);
        return productStockEntity;
    }
}
