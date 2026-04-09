package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdGreaterThanCpedReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotIncludeCipsdReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodsNotContainingSincePerformanceStartDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodsNotContainingYearToDateReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.RollingPeriodsLessThan12ReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.RollingPeriodsReqValidation;
import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import com.fintex.ce.domain.model.holding.Holding;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.fintex.sm.model.domain.enumeration.CurrencyType.CAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RollingCalculationReqDtoValidatorTest {

  @Test
  void build_checkResult() {
    final var sut = new RollingCalculationReqDtoValidator();

    final RollingCalculationCommand reqDTO = getRollingCalculationCommand();

    final ReqValidation expected = ReqValidation.create()
        .linkWith(new NotNullReqValidation(reqDTO))
        .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
        .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPsd()))
        .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPed()))
        .linkWith(new CpsdGreaterThanCpedReqValidation(reqDTO.getCustomPsd(), reqDTO.getCustomPed()))
        .linkWith(new NotIncludeCipsdReqValidation(reqDTO.getCustomIntervalPsd()))
        .linkWith(new PeriodsNotContainingYearToDateReqValidation(reqDTO.getRollingPeriods()))
        .linkWith(new PeriodsNotContainingSincePerformanceStartDateReqValidation(reqDTO.getRollingPeriods()))
        .linkWith(new PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation(reqDTO
            .getRollingPeriods()))
        .linkWith(new RollingPeriodsReqValidation(reqDTO.getRollingPeriods()))
        .linkWith(new RollingPeriodsLessThan12ReqValidation(reqDTO.getRollingPeriods()))
        .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));

    final ReqValidation actual = sut.build(reqDTO);

    assertEquals(expected, actual);
  }

  static RollingCalculationCommand getRollingCalculationCommand() {
    final var reqDTO = new RollingCalculationCommand();
    reqDTO.setCurrency(CAD);
    reqDTO.setCustomPsd(LocalDate.now());
    reqDTO.setCustomPed(LocalDate.now().plusMonths(1));
    reqDTO.setCustomIntervalPsd(LocalDate.now().minusMonths(1));
    reqDTO.setRollingPeriods(Set.of("1", "2", "3"));
    reqDTO.setHoldings(List.of(mock(Holding.class)));
    reqDTO.setBenchmarkHoldings(List.of(mock(Holding.class), mock(Holding.class)));
    return reqDTO;
  }

}