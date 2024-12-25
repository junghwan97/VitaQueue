package com.example.vitaqueue.order.domain.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {

    CREATED("신규 주문"),
    CANCELLED("주문 취소"),
    COMPLETED("주문 완료"),
    DELIVERY("배송중"),
    DELIVERED("배송 완료"),
    RETURNED("반품 처리"),
    RETURN_REQUESTED("반품 요청");

    private String value;

    OrderStatus(String value) {
        this.value = value;
    }
}