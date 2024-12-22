package com.example.vitaqueue.product.dto.response;

import com.example.vitaqueue.product.model.entity.ProductEntity;
import com.example.vitaqueue.security.EncryptionUtil;
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
    private String seller;
    private BigDecimal price;
    private String descript;
    private Long stock;

    public static ProductResponse fromEntity(ProductEntity product) throws Exception {
        ProductResponse response = new ProductResponse();
        response.id = product.getId();
        response.name = product.getName();
        response.seller = EncryptionUtil.decrypt(product.getUserId().getName());
        response.price = product.getPrice();
        response.descript = product.getDescript();
        return response;
    }

    public static ProductResponse fromEntity(ProductEntity product, Long stock) throws Exception {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                EncryptionUtil.decrypt(product.getUserId().getName()),
                product.getPrice(),
                product.getDescript(),
                stock
        );
    }
}
