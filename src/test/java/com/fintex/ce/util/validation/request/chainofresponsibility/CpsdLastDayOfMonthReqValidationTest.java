package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_CPSD_001;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpsdLastDayOfMonthReqValidationTest {

    @Test
    void check_cpsdIsNotLastDayOfMonth() {
        //SETUP
        final var sut = new CpsdLastDayOfMonthReqValidation(LocalDate.of(2000, 5, 5));

        final ReqValidationException expected = ERR_RRC_CPSD_001.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void check_validCase() {
        //SETUP
        final var sut = new CpsdLastDayOfMonthReqValidation(LocalDate.of(2000, 5, 31));

        //ACT
        sut.check();

        //VERIFY
    }


}