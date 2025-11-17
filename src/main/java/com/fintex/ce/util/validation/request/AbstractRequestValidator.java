package com.fintex.ce.util.validation.request;

import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;

public abstract class AbstractRequestValidator<T> implements RequestValidator<T> {

    @Override
    public void validate(T t) {
        build(t).validate();
    }

    public abstract ReqValidation build(T t);
}
