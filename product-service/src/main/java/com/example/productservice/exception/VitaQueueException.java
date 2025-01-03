package com.example.productservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VitaQueueException extends RuntimeException{
    private ErrorCode errorCode;
    private String message;

    @Override
    public String getMessage() {
        if (message == null) return errorCode.getMessage();

        return String.format("%s. %s", errorCode.getMessage(), message);
    }

    public VitaQueueException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = null;
    }

}
