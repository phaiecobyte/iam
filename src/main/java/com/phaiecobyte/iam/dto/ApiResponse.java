package com.phaiecobyte.iam.dto;

import com.phaiecobyte.iam.exception.ErrorCode;

public class ApiResponse<T>{
    private boolean status;
    private String code;
    private String message;
    private T data;
    private Object errors;

    private ApiResponse(
            boolean status,
            String code,
            String message,
            T data,
            Object errors
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    /**
     * Success response
     */
    public static <T> ApiResponse<T> apiSuccessRes(
            boolean status,
            String message,
            T data
    ) {
        return new ApiResponse<>(
                status,
                null,
                message,
                data,
                null
        );
    }

    /**
     * Error response
     */
    public static <T> ApiResponse<T> apiErrorRes(
            ErrorCode errorCode,
            String message,
            Object errors
    ) {
        return new ApiResponse<>(
                false,
                errorCode.name(),
                message,
                null,
                errors
        );
    }

}
