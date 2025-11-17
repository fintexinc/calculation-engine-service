package com.fintex.ce.util.validation.request.chainofresponsibility;

import lombok.EqualsAndHashCode;

import java.util.Objects;

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
