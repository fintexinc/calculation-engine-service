package com.fintex.ce.util.validation.request;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.BestWorstPeriodsReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.BestWorstPeriodReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpsdGreaterThanCpedReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.util.validation.request.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.util.validation.request.chainofresponsibility.ReqValidation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class BestWorstPeriodsReqValidatorTest {

    @Test
    void build_checkResult() {
        //SETUP
        final var sut = new BestWorstPeriodsReqValidator();

        final BestWorstPeriodsReqDTO reqDTO = getBestWorstPeriodsReqDTO();

        final ReqValidation expected = ReqValidation.create()
                .linkWith(new NotNullReqValidation(reqDTO))
                .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
                .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPerformanceStartDate()))
                .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPerformanceEndDate()))
                .linkWith(new CpsdGreaterThanCpedReqValidation(reqDTO.getCustomPerformanceStartDate(), reqDTO.getCustomPerformanceEndDate()))
                .linkWith(new BestWorstPeriodReqValidation(reqDTO.getBestWorstTimeIntervalPeriods()))
                .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
                .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));

        //ACT
        final ReqValidation actual = sut.build(reqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

    BestWorstPeriodsReqDTO getBestWorstPeriodsReqDTO() {
        final BestWorstPeriodsReqDTO reqDTO = new BestWorstPeriodsReqDTO();
        reqDTO.setCurrency(Currency.CAD);
        reqDTO.setCustomPerformanceStartDate(LocalDate.of(2000, 5, 31));
        reqDTO.setCustomPerformanceEndDate(LocalDate.of(2020, 4, 30));
        reqDTO.setBestWorstTimeIntervalPeriods(Set.of(1L, 2L, 3L));
        reqDTO.setHoldings(List.of(mock(Holding.class)));
        return reqDTO;
    }

}