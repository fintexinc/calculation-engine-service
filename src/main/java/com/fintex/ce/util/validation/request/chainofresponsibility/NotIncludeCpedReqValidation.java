package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class NotIncludeCpedReqValidation extends NotIncludePropertyAbstractReqValidation {
    public NotIncludeCpedReqValidation(Object property) {
        super(property);
    }

    @Override
    public void throwException() {
        throw ExceptionCode.ERR_RRC_TIP_006.reqValidationError();
    }
}
