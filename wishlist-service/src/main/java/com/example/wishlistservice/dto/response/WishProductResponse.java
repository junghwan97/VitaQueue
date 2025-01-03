package com.example.wishlistservice.dto.response;

import com.example.wishlistservice.model.entity.WishProduct;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class WishProductResponse {
    private Long wishProductId;
    private Long productId;
    private Long userId;
    private int quantity;
    private BigDecimal price;

    public static WishProductResponse fromEntity(WishProduct entity) {
        return new WishProductResponse(
                entity.getId(),
                entity.getProductId(),
                entity.getUserId(),
                entity.getProductQuantity(),
                entity.getProductPrice()
        );
    }
}
