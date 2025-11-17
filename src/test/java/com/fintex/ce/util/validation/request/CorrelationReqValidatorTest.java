package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.CipsdGreaterThanCpedReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CipsdLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodContainYearToDateReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodLessThan12ReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.PeriodReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.util.validation.request.TrailingTotalReturnsReqValidatorTest.getPeriodsReqDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CorrelationReqValidatorTest {

    @Test
    void build_checkResult() {
        //SETUP
        final var sut = new CorrelationReqValidator();

        final PeriodsReqDTO reqDTO = getPeriodsReqDTO();

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
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()));

        //ACT
        final ReqValidation actual = sut.build(reqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

}