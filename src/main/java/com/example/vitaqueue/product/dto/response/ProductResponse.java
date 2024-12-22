package com.example.vitaqueue.product.dto.response;

import com.example.vitaqueue.product.model.entity.ProductEntity;
import com.example.vitaqueue.security.EncryptionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String seller;
    private BigDecimal price;
    private String descript;

    public static ProductResponse fromEntity(ProductEntity product) throws Exception {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                EncryptionUtil.decrypt(product.getUserId().getName()),
                product.getPrice(),
                product.getDescript()
        );
    }
}
