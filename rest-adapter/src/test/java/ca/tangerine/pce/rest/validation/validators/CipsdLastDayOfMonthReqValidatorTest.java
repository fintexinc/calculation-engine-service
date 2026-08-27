package ca.tangerine.pce.rest.validation.validators;

import java.time.LocalDate;

import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.rest.validation.RequestValidator;
import ca.tangerine.wm.commons.domain.currency.Currency;

class CipsdLastDayOfMonthReqValidatorTest extends AbstractLastDayOfMonthReqValidatorTest {

  @Override
  RequestValidator createValidator() {
    return new CipsdLastDayOfMonthReqValidator();
  }

  @Override
  CalculationCommand createCommandWithDate(LocalDate date) {
    PeriodCommand command = new PeriodCommand();
    command.setCustomIntervalPsd(date);
    command.setCurrency(Currency.CAD);
    return command;
  }

  @Override
  String expectedErrorCode() {
    return "CIPSD_NOT_MONTH_END";
  }
}
