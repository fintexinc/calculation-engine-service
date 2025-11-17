package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.exception.ReqValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_MC_001;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotEmptyCurrencyReqValidatorTest {

    @Test
    void check_periodIsLessThanZero() {
        //SETUP
        final var sut = new NotEmptyCurrencyReqValidator(null);

        final ReqValidationException expected = ERR_RRC_MC_001.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void check_validCase() {
        //SETUP
        final var sut = new NotEmptyCurrencyReqValidator(Currency.CAD);

        //ACT
        Assertions.assertDoesNotThrow(sut::check);

        //VERIFY
    }


}