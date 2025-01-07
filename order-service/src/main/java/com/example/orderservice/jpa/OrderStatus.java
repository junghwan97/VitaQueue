package com.example.orderservice.jpa;

import lombok.Getter;

@Getter
public enum OrderStatus {

    CREATED("신규 주문"),
    CANCELLED("주문 취소"),
    COMPLETED("주문 완료"),
    DELIVERY("배송중"),
    DELIVERED("배송 완료"),
    RETURNED("반품 처리"),
    RETURN_REQUESTED("반품 요청"),
    PAYMENT_ENTERED("결제 진입"),
    PAYMENT_SUCCESS("결제 완료"),
    PAYMENT_FAILED("결제 실패");

    private String value;

    OrderStatus(String value) {
        this.value = value;
    }
}