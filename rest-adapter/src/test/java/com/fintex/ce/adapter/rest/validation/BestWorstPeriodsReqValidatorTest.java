package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.dto.request.BestWorstPeriodsReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.BestWorstPeriodReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdGreaterThanCpedReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class BestWorstPeriodsReqValidatorTest {

  @Test
  void build_checkResult() {
    final var sut = new BestWorstPeriodsReqValidator();

    final BestWorstPeriodsReqDTO reqDTO = getBestWorstPeriodsReqDTO();

    final ReqValidation expected = ReqValidation.create()
        .linkWith(new NotNullReqValidation(reqDTO))
        .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
        .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPerformanceStartDate()))
        .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPerformanceEndDate()))
        .linkWith(new CpsdGreaterThanCpedReqValidation(reqDTO.getCustomPerformanceStartDate(), reqDTO
            .getCustomPerformanceEndDate()))
        .linkWith(new BestWorstPeriodReqValidation(reqDTO.getBestWorstTimeIntervalPeriods()))
        .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));

    final ReqValidation actual = sut.build(reqDTO);

    assertEquals(expected, actual);
  }

  BestWorstPeriodsReqDTO getBestWorstPeriodsReqDTO() {
    final BestWorstPeriodsReqDTO reqDTO = new BestWorstPeriodsReqDTO();
    reqDTO.setCurrency(CurrencyType.CAD);
    reqDTO.setCustomPerformanceStartDate(LocalDate.of(2000, 5, 31));
    reqDTO.setCustomPerformanceEndDate(LocalDate.of(2020, 4, 30));
    reqDTO.setBestWorstTimeIntervalPeriods(Set.of(1L, 2L, 3L));
    reqDTO.setHoldings(List.of(mock(Holding.class)));
    return reqDTO;
  }

}