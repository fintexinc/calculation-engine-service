package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_RTIP_001;
import static org.junit.jupiter.api.Assertions.*;

class RollingPeriodsLessThan12ReqValidationTest {

    @Test
    void check_periodLessThan12() {
        //SETUP
        final var sut = new RollingPeriodsLessThan12ReqValidation(
                Set.of("1", "2"));

        final ReqValidationException expected = ERR_RRC_RTIP_001.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void check_validCase() {
        //SETUP
        final var sut = new RollingPeriodsLessThan12ReqValidation(Set.of());

        //ACT
        assertDoesNotThrow(sut::check);

        //VERIFY
    }


}