package com.fintex.ce.exception.code;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    OK(HttpStatus.OK),
    CREATED(HttpStatus.CREATED),
    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    CONFLICT(HttpStatus.CONFLICT);

    private final HttpStatus httpStatus;

    ErrorCode(final HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
