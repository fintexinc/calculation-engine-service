package com.fintex.ce.adapter.rest.validation;

public interface RequestValidator<T> {
  void validate(T t);
}
