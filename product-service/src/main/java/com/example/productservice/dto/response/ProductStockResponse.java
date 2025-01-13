package com.example.productservice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockResponse {
    private Long id;

    private Long productId;

    private Integer stock;

//    // 재고 증가
//    public void increaseStock(Long quantity) {
//        if (quantity < 0) {
//            throw new VitaQueueException(ErrorCode.STOCK_INCREASE_NEGATIVE, "재고 증가 수량은 음수일 수 없습니다.");
//        }
//        this.stock += quantity;
//    }
//
//    // 재고 감소
//    public void decreaseStock(Long quantity) {
//        if (quantity < 0) {
//            throw new VitaQueueException(ErrorCode.STOCK_DECREASE_NEGATIVE, "재고 감소 수량은 음수일 수 없습니다.");
//        }
//        if (this.stock < quantity) {
//            throw new VitaQueueException(ErrorCode.STOCK_NOT_ENOUGH, "재고가 부족합니다.");
//        }
//        this.stock -= quantity;
//    }
}
