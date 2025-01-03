package com.example.productservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."), // 사용자가 존재하지 않을 때
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "중복된 이메일 주소입니다."), // 이미 사용 중인 이메일 주소일 때
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."), // 비밀번호가 잘못 입력되었을 때
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."), // 예기치 못한 서버 오류
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."), // 요청한 상품이 존재하지 않을 때
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "권한이 유효하지 않습니다."), // 권한이 부족하거나 유효하지 않을 때
    WISH_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "위시리스트 상품을 찾을 수 없습니다."), // 위시리스트에 해당 상품이 없을 때
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."), // 요청한 주문이 존재하지 않을 때
    INVALID_ORDER_STATE(HttpStatus.BAD_REQUEST, "잘못된 주문 상태입니다."), // 주문 상태가 잘못되었을 때
    RETURN_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "반품 가능 기간이 만료되었습니다."), // 반품 요청 기간이 지났을 때
    PRODUCT_STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "상품 재고를 찾을 수 없습니다."), // 상품 재고 정보가 없을 때
    ORDER_CANCELLATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "주문 취소가 허용되지 않습니다."), // 주문 취소가 불가능할 때
    STOCK_INCREASE_NEGATIVE(HttpStatus.BAD_REQUEST, "재고 증가 수량은 음수일 수 없습니다."), // 재고 증가 값이 음수일 때
    STOCK_DECREASE_NEGATIVE(HttpStatus.BAD_REQUEST, "재고 감소 수량은 음수일 수 없습니다."), // 재고 감소 값이 음수일 때
    STOCK_NOT_ENOUGH(HttpStatus.CONFLICT, "재고가 부족합니다."); // 재고가 요청 수량보다 적을 때


    private HttpStatus status;
    private String message;
}