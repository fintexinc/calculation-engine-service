package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_TIP_005;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotIncludeCipsdReqValidationTest {

    @Test
    void check_throwException() {
        //SETUP
        final var sut = new NotIncludeCipsdReqValidation(new Object());

        final ReqValidationException expected = ERR_RRC_TIP_005.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
    }

}