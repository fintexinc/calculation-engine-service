package com.fintex.ce.domain.exception.code;

public enum HttpCode {

  OK(200),
  CREATED(201),
  BAD_REQUEST(400),
  UNAUTHORIZED(401),
  FORBIDDEN(403),
  NOT_FOUND(404),
  INTERNAL_SERVER_ERROR(500),
  CONFLICT(409);

  private final int httpStatusCode;

  HttpCode(final int httpStatusCode) {
    this.httpStatusCode = httpStatusCode;
  }

  public int getHttpStatusCode() {
    return httpStatusCode;
  }
}
