package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_RTIP_003;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_TIP_004;
import static org.junit.jupiter.api.Assertions.*;

class RollingPeriodsReqValidationTest {

    @Test
    void check_periodIsNotAllowed() {
        //SETUP
        final var sut = new RollingPeriodsReqValidation(Set.of("abc"));

        final ReqValidationException expected = ERR_RRC_TIP_004.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void check_periodIsLessThanZero() {
        //SETUP
        final var sut = new RollingPeriodsReqValidation(Set.of("-1"));

        final ReqValidationException expected = ERR_RRC_RTIP_003.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void check_validCase() {
        //SETUP
        final var sut = new RollingPeriodsReqValidation(Set.of());

        //ACT
        assertDoesNotThrow(sut::check);

        //VERIFY
    }

}