package com.fintex.ce.util.validation.request;

public interface RequestValidator<T> {
    void validate(T t);
}
