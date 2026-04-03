package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CipsdGreaterThanCpedReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CipsdLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyGicInterestRateReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.holding.Holding;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static com.fintex.sm.model.domain.enumeration.CurrencyType.CAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class TrailingTotalReturnsReqValidatorTest {

  @Test
  void build_checkResult() {
    final var sut = new TrailingTotalReturnsReqValidator();

    final var reqDTO = getPeriodCommand();

    final ReqValidation expected = ReqValidation.create()
        .linkWith(new NotNullReqValidation(reqDTO))
        .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
        .linkWith(new CipsdLastDayOfMonthReqValidation(reqDTO.getCustomIntervalPsd()))
        .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPed()))
        .linkWith(new CipsdGreaterThanCpedReqValidation(reqDTO.getCustomIntervalPsd(), reqDTO.getCustomPed()))
        .linkWith(new PeriodReqValidation(reqDTO.getPeriods()))
        .linkWith(new NotEmptyGicInterestRateReqValidator(reqDTO.getHoldings()))
        .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));

    final ReqValidation actual = sut.build(reqDTO);

    assertEquals(expected, actual);
  }

  static PeriodCommand getPeriodCommand() {
    final var reqDTO = new PeriodCommand();
    reqDTO.setCurrency(CAD);
    reqDTO.setPeriods(Set.of("1", "2", "3"));
    reqDTO.setCustomIntervalPsd(LocalDate.of(2019, 5, 31));
    reqDTO.setCustomPed(LocalDate.of(2020, 5, 31));
    reqDTO.setHoldings(List.of(mock(Holding.class)));
    return reqDTO;
  }

}