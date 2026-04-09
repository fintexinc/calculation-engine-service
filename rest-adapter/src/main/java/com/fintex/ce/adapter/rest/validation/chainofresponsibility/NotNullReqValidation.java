package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import java.util.Objects;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class NotNullReqValidation extends ReqValidation {

  public static final String REQUEST_COULD_NOT_BE_NULL = "Request could not be null";

  private final Object request;

  public NotNullReqValidation(final Object request) {
    this.request = request;
  }

  @Override
  public void check() {
    Objects.requireNonNull(request, REQUEST_COULD_NOT_BE_NULL);
  }
}
