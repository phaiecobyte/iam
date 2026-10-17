package com.phaiecobyte.iam.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    E_0001(HttpStatus.UNAUTHORIZED,"ERR-0001","Invalid username or password"),
    CLIENT_0001(HttpStatus.CONFLICT,"CLIENT-ERR001","User already exist"),
    CLIENT_0002(HttpStatus.BAD_REQUEST,"CLIENT-ERR002","Invalid or expired email verification token");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

}
