package com.example.edu_digization.exception;

import lombok.Getter;

/**
 * 业务异常：用于在业务逻辑中抛出可预期的错误
 * 由 GlobalExceptionHandler 统一转换为 Result
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码，默认 500 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
