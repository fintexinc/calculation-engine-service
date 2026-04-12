package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

import java.time.LocalDate;

class CipsdLastDayOfMonthReqValidatorTest extends AbstractLastDayOfMonthReqValidatorTest {

  @Override
  RequestValidator createValidator() {
    return new CipsdLastDayOfMonthReqValidator();
  }

  @Override
  CalculationCommand createCommandWithDate(LocalDate date) {
    PeriodCommand command = new PeriodCommand();
    command.setCustomIntervalPsd(date);
    command.setCurrency(CurrencyType.CAD);
    return command;
  }

  @Override
  String expectedErrorCode() {
    return "ERR_RRC_CIPSD_001";
  }
}
