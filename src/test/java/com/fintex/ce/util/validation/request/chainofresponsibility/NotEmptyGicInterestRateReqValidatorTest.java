package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_GIC_MC_001;
import static org.junit.jupiter.api.Assertions.*;

class NotEmptyGicInterestRateReqValidatorTest {

    @Test
    void check_ERR_GIC_MC_001ThrownWhenInterestRateIsNull() {
        //SETUP
        final var sut = new NotEmptyGicInterestRateReqValidator(List.of(new GicHolding()));

        final ReqValidationException expected = ERR_GIC_MC_001.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void check_nothingThrownIfInterestRateIsEntered() {
        //SETUP
        final GicHolding gic = new GicHolding();
        gic.setClientIntRate(BigDecimal.ONE);
        final var sut = new NotEmptyGicInterestRateReqValidator(List.of(gic));

        //ACT
        assertDoesNotThrow(() -> sut.check());

        //VERIFY
    }

}
