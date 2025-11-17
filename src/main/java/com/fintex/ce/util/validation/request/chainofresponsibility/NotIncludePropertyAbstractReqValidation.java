package com.fintex.ce.util.validation.request.chainofresponsibility;

import lombok.EqualsAndHashCode;

import static java.util.Objects.nonNull;

@EqualsAndHashCode(callSuper = true)
public abstract class NotIncludePropertyAbstractReqValidation extends ReqValidation {

    private final Object property;

    public NotIncludePropertyAbstractReqValidation(final Object property) {
        this.property = property;
    }

    @Override
    public void check() {
        if (nonNull(property)) {
            throwException();
        }
    }

    public abstract void throwException();
}
