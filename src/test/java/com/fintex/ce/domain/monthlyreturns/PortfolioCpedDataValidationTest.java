package com.fintex.ce.domain.monthlyreturns;

import com.fintex.ce.config.enumeration.ExceptionCode;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_CPED_002;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_CPED_003;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PortfolioCpedDataValidationTest {

    @Test
    void getCpedIsAfterPedExceptionCode_checkResult() {
        //SETUP
        final PortfolioCpedDataValidation sut = new PortfolioCpedDataValidation();

        //ACT
        final ExceptionCode actual = sut.getCpedIsAfterPedExceptionCode();

        //VERIFY
        assertEquals(ERR_RRC_CPED_003, actual);
    }

    @Test
    void getCpedIsBeforePsdExceptionCode_checkResult() {
        //SETUP
        final PortfolioCpedDataValidation sut = new PortfolioCpedDataValidation();

        //ACT
        final ExceptionCode actual = sut.getCpedIsBeforePsdExceptionCode();

        //VERIFY
        assertEquals(ERR_RRC_CPED_002, actual);
    }

}