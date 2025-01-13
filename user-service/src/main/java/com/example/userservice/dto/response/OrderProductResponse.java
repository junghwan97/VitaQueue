package com.example.userservice.dto.response;

import com.example.userservice.jpa.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderProductResponse {

    private Long id;
    private OrderStatus orderStatus;
    private String productName;
    private Integer quantity ;
    private BigDecimal price;


}
