package com.example.orderservice.dto.response;


import com.example.orderservice.jpa.OrderProductEntity;
import com.example.orderservice.jpa.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderProductResponse {

    private Long id;
    private OrderStatus orderStatus;
    private String productName;
    private int quantity ;
    private BigDecimal price;

    public static OrderProductResponse fromEntity(OrderProductEntity orderProduct, String name) {

        return new OrderProductResponse(
                orderProduct.getId(),
                orderProduct.getStatus(),
                name,
                orderProduct.getQuantity(),
                orderProduct.getPrice()
        );
    }
}
