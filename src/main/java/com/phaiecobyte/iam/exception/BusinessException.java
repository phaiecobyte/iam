package com.phaiecobyte.iam.exception;

import lombok.Getter;


@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object errors;

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public BusinessException(
            ErrorCode errorCode,
            String message,
            Object errors
    ) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors;
    }

}
