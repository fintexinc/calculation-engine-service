package com.fintex.ce.util.validation.request;

import com.fintex.ce.util.validation.request.chainofresponsibility.BenchmarksCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpsdGreaterThanCpedReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotIncludeCipsdReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodsNotContainingSincePerformanceStartDateReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodsNotContainingYearToDateReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.RollingPeriodsReqValidation;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.util.validation.request.RollingCalculationReqDtoValidatorTest.getRollingCalculationReqDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RollingCorrelationReqValidatorTest {

    @Test
    void build_checkResult() {
        //SETUP
        final var sut = new RollingCorrelationReqValidator();

        final var reqDTO = getRollingCalculationReqDTO();

        final ReqValidation expected = ReqValidation.create()
                .linkWith(new NotNullReqValidation(reqDTO))
                .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
                .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPsd()))
                .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPed()))
                .linkWith(new CpsdGreaterThanCpedReqValidation(reqDTO.getCustomPsd(), reqDTO.getCustomPed()))
                .linkWith(new NotIncludeCipsdReqValidation(reqDTO.getCustomIntervalPsd()))
                .linkWith(new PeriodsNotContainingYearToDateReqValidation(reqDTO.getRollingPeriods()))
                .linkWith(new PeriodsNotContainingSincePerformanceStartDateReqValidation(reqDTO.getRollingPeriods()))
                .linkWith(new PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation(reqDTO.getRollingPeriods()))
                .linkWith(new RollingPeriodsReqValidation(reqDTO.getRollingPeriods()))
                .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
                .linkWith(new BenchmarksCouldNotBeEmptyReqValidation(reqDTO.getBenchmarkHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getBenchmarkHoldings()));

        //ACT
        final ReqValidation actual = sut.build(reqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

}