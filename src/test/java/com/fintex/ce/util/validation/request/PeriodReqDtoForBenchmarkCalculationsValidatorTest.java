package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.util.validation.request.chainofresponsibility.BenchmarksCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CipsdGreaterThanCpedReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CipsdLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodContainYearToDateReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodLessThan12ReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PeriodReqDtoForBenchmarkCalculationsValidatorTest {

    @Test
    void build_checkResult() {
        //SETUP
        final var sut = new PeriodReqDtoForBenchmarkCalculationsValidator(new PeriodsReqDtoValidator());

        final var reqDTO = TrailingTotalReturnsReqValidatorTest.getPeriodsReqDTO();
        reqDTO.setBenchmarkHoldings(List.of(mock(Holding.class)));

        final ReqValidation expected = ReqValidation.create()
                .linkWith(new NotNullReqValidation(reqDTO))
                .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
                .linkWith(new CipsdLastDayOfMonthReqValidation(reqDTO.getCustomIntervalPsd()))
                .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPed()))
                .linkWith(new CipsdGreaterThanCpedReqValidation(reqDTO.getCustomIntervalPsd(), reqDTO.getCustomPed()))
                .linkWith(new PeriodReqValidation(reqDTO.getPeriods()))
                .linkWith(new PeriodLessThan12ReqValidation(reqDTO.getPeriods()))
                .linkWith(new PeriodContainYearToDateReqValidation(reqDTO.getPeriods()))
                .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()))
                .linkWith(new BenchmarksCouldNotBeEmptyReqValidation(reqDTO.getBenchmarkHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getBenchmarkHoldings()))
                .linkWith(new HoldingValueReqValidator(reqDTO.getBenchmarkHoldings()));

        //ACT
        final ReqValidation actual = sut.build(reqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }


}