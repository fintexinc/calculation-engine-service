package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_CPSD_004;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpsdGreaterThanCpedReqValidationTest {

    @Test
    void check_cpsdGreaterThanCped() {
        //SETUP
        final var sut = new CpsdGreaterThanCpedReqValidation(LocalDate.now(), LocalDate.now().minusMonths(1));

        final ReqValidationException expected = ERR_RRC_CPSD_004.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void check_validCase() {
        //SETUP
        final var sut = new CpsdGreaterThanCpedReqValidation(LocalDate.now(), LocalDate.now().plusMonths(1));

        //ACT
        sut.check();

        //VERIFY
    }

}