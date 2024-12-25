package com.example.vitaqueue.orderProduct.dto.response;

import com.example.vitaqueue.order.domain.enums.OrderStatus;
import com.example.vitaqueue.orderProduct.domain.entity.OrderProductEntity;
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
