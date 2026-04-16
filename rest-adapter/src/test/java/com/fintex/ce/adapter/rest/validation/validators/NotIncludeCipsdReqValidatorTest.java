package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.currency.Currency;

import java.time.LocalDate;

class NotIncludeCipsdReqValidatorTest extends AbstractNotIncludePropertyReqValidatorTest {

  @Override
  RequestValidator createValidator() {
    return new NotIncludeCipsdReqValidator();
  }

  @Override
  CalculationCommand createCommandWithProperty() {
    PeriodCommand command = new PeriodCommand();
    command.setCustomIntervalPsd(LocalDate.of(2025, 1, 31));
    command.setCurrency(Currency.CAD);
    return command;
  }

  @Override
  CalculationCommand createCommandWithoutProperty() {
    PeriodCommand command = new PeriodCommand();
    command.setCustomIntervalPsd(null);
    command.setCurrency(Currency.CAD);
    return command;
  }

  @Override
  String expectedErrorCode() {
    return "ERR_RRC_TIP_005";
  }
}
