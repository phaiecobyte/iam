package com.phaiecobyte.iam.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    CLIENT_0001(HttpStatus.CONFLICT,"CLIENT-0001","User already exist"),
    CLIENT_0002(HttpStatus.BAD_REQUEST,"CLIENT-0002","Invalid or expired email verification token"),
    CLIENT_0003(HttpStatus.UNAUTHORIZED,"CLIENT-0003","Invalid email or password"),
    CLIENT_0004(HttpStatus.LOCKED,"CLIENT-0004","Account temporarily locked. Try again later."),
    CLIENT_0005(HttpStatus.FORBIDDEN,"CLIENT-0005","Account is not eligible to sign in"),
    CLIENT_0006(HttpStatus.BAD_REQUEST,"CLIENT-0006","Invalid input"),
    CLIENT_0007(HttpStatus.NOT_FOUND,"CLIENT-404","Resources not found");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

}
