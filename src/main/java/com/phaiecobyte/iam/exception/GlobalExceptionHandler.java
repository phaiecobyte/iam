package com.phaiecobyte.iam.exception;

import com.phaiecobyte.iam.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessExceptions(BusinessException ex, HttpServletRequest request) {
        log.error("handleBusinessExceptions:{}",ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().status())
                .body(
                        ApiResponse.apiErrorRes(
                                ex.getErrorCode(),
                                ex.getMessage(),
                                ex.getErrors()
                        )
                );
    }


}
