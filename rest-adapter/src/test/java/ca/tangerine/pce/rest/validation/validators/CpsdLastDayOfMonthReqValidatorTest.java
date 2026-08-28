package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.ReturnCommand;
import ca.tangerine.pce.rest.validation.RequestValidator;
import ca.tangerine.wm.commons.domain.currency.Currency;

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
