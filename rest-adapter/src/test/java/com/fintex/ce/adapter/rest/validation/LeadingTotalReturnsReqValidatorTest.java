package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.rest.dto.request.LeadingTotalReturnPeriodsReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotIncludeCipsdReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotIncludeCpedReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodsNotContainingSincePerformanceStartDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodsNotContainingYearToDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.fintex.ce.domain.model.enumeration.Currency.CAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class LeadingTotalReturnsReqValidatorTest {

  @Test
  void build_checkResult() {
    // SETUP
    final var sut = new LeadingTotalReturnsReqValidator();

    final var reqDTO = getPeriodsReqDTO();

    final ReqValidation expected = ReqValidation.create()
        .linkWith(new NotNullReqValidation(reqDTO))
        .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
        .linkWith(new NotIncludeCipsdReqValidation(reqDTO.getCustomIntervalPsd()))
        .linkWith(new NotIncludeCpedReqValidation(reqDTO.getCustomPed()))
        .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPsd()))
        .linkWith(new PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation(reqDTO.getPeriods()))
        .linkWith(new PeriodsNotContainingSincePerformanceStartDateReqValidation(reqDTO.getPeriods()))
        .linkWith(new PeriodsNotContainingYearToDateReqValidation(reqDTO.getPeriods()))
        .linkWith(new PeriodReqValidation(reqDTO.getPeriods()))
        .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));
    // ACT
    final ReqValidation actual = sut.build(reqDTO);

    // VERIFY
    assertEquals(expected, actual);
  }

  static LeadingTotalReturnPeriodsReqDTO getPeriodsReqDTO() {
    final var reqDTO = new LeadingTotalReturnPeriodsReqDTO();
    reqDTO.setCurrency(CAD);
    reqDTO.setPeriods(Set.of("1", "2", "3"));
    reqDTO.setCustomIntervalPsd(LocalDate.of(2019, 5, 31));
    reqDTO.setCustomPed(LocalDate.of(2020, 5, 31));
    reqDTO.setCustomPsd(LocalDate.of(2000, 3, 31));
    reqDTO.setHoldings(List.of(mock(Holding.class)));
    return reqDTO;
  }

}