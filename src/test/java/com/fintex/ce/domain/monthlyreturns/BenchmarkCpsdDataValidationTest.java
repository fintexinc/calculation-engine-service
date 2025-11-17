package com.fintex.ce.domain.monthlyreturns;

import com.fintex.ce.config.enumeration.ExceptionCode;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_BMPSD_002;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_BMPSD_003;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BenchmarkCpsdDataValidationTest {

    @Test
    void getCpsdIsBeforePsdExceptionCode_checkResult() {
        //SETUP
        final BenchmarkCpsdDataValidation sut = new BenchmarkCpsdDataValidation();

        //ACT
        final ExceptionCode actual = sut.getCpsdIsBeforePsdExceptionCode();

        //VERIFY
        assertEquals(ERR_RRC_BMPSD_002, actual);
    }

    @Test
    void getCpsdIsAfterPedExceptionCode_checkResult() {
        //SETUP
        final BenchmarkCpsdDataValidation sut = new BenchmarkCpsdDataValidation();

        //ACT
        final ExceptionCode actual = sut.getCpsdIsAfterPedExceptionCode();

        //VERIFY
        assertEquals(ERR_RRC_BMPSD_003, actual);
    }


}