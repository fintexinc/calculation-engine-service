package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;

public abstract class AbstractRequestValidator<T> implements RequestValidator<T> {

  @Override
  public void validate(T t) {
    build(t).validate();
  }

  public abstract ReqValidation build(T t);
}
