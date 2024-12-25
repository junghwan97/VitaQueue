package com.example.vitaqueue.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User Not Found"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "Duplicate Email"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid Password"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product doesn't exist"),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "Permission is invalid"),
    WISH_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "WishProduct is doesn't exist"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order Not Found"),
    INVALID_ORDER_STATE(HttpStatus.BAD_REQUEST, "Invalid Order State"),
    RETURN_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "Return period has expired"),
    PRODUCT_STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "Product Stock Not Found")
    ;

    private HttpStatus status;
    private String message;
}