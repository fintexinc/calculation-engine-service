package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.wm.commons.domain.currency.Currency;

import java.time.LocalDate;

class CpsdLastDayOfMonthReqValidatorTest extends AbstractLastDayOfMonthReqValidatorTest {

  private final CpsdLastDayOfMonthReqValidator validator = new CpsdLastDayOfMonthReqValidator();

  @Override
  RequestValidator createValidator() {
    return validator;
  }

  @Override
  CalculationCommand createCommandWithDate(LocalDate date) {
    ReturnCommand command = new ReturnCommand();
    command.setCustomPsd(date);
    command.setCurrency(Currency.CAD);
    return command;
  }

  @Override
  String expectedErrorCode() {
    return "CPSD_NOT_MONTH_END";
  }
}
