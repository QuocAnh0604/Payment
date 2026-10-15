package com.example.payment.payment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public static BusinessException badRequest(String code, String msg) {
        return new BusinessException(code, msg, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException notFound(String code, String msg) {
        return new BusinessException(code, msg, HttpStatus.NOT_FOUND);
    }
}
