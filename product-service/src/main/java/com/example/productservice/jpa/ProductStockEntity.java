package com.example.productservice.jpa;

import com.example.productservice.exception.ErrorCode;
import com.example.productservice.exception.VitaQueueException;
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
    private Long stock;

    public ProductStockEntity of(Long productId, Long stock) {
        ProductStockEntity productStockEntity = new ProductStockEntity();
        productStockEntity.setProductId(productId);
        productStockEntity.setStock(stock);
        return productStockEntity;
    }

     // 재고 증가
    public void increaseStock(Long quantity) {
        if (quantity < 0) {
            throw new VitaQueueException(ErrorCode.STOCK_INCREASE_NEGATIVE, "재고 증가 수량은 음수일 수 없습니다.");
        }
        this.stock += quantity;
    }

    // 재고 감소
    public void decreaseStock(Long quantity) {
        if (quantity < 0) {
            throw new VitaQueueException(ErrorCode.STOCK_DECREASE_NEGATIVE, "재고 감소 수량은 음수일 수 없습니다.");
        }
        if (this.stock < quantity) {
            throw new VitaQueueException(ErrorCode.STOCK_NOT_ENOUGH, "재고가 부족합니다.");
        }
        this.stock -= quantity;
    }
}
