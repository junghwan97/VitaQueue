package com.example.productservice.dto.response;

import com.example.productservice.jpa.ProductEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private Long sellerId;
    private BigDecimal price;
    private String descript;
    private Long stock;

    public static ProductResponse fromEntity(ProductEntity product){
        ProductResponse response = new ProductResponse();
        response.id = product.getId();
        response.name = product.getName();
        response.sellerId = product.getUserId();
        response.price = product.getPrice();
        response.descript = product.getDescript();
        return response;
    }

    public static ProductResponse fromEntity(ProductEntity product, Long stock){
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getUserId(),
                product.getPrice(),
                product.getDescript(),
                stock
        );
    }
}
