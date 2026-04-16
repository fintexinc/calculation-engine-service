package com.fintex.ce.model.error;

import lombok.Getter;

// TODO must be deleted at TMI-316 as http codes are not in the area of responsibility of the domain module
@Getter
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

}
