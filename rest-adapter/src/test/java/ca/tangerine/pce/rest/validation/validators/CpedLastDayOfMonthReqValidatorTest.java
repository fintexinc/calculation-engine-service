package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.dto.command.ReturnCommand;
import ca.tangerine.pce.model.error.exceptions.ValidationException;
import ca.tangerine.pce.rest.validation.RequestValidator;
import ca.tangerine.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpedLastDayOfMonthReqValidatorTest extends AbstractLastDayOfMonthReqValidatorTest {

  private final CpedLastDayOfMonthReqValidator validator = new CpedLastDayOfMonthReqValidator();

  @Override
  RequestValidator createValidator() {
    return validator;
  }

  @Override
  CalculationCommand createCommandWithDate(LocalDate date) {
    PeriodCommand command = new PeriodCommand();
    command.setCustomPed(date);
    command.setCurrency(Currency.CAD);
    return command;
  }

  @Override
  String expectedErrorCode() {
    return "CPED_NOT_MONTH_END";
  }

  @Test
  void shouldNotThrow_whenReturnCommandDateIsLastDayOfMonth() {
    ReturnCommand command = new ReturnCommand();
    command.setCustomPed(LocalDate.of(2025, 1, 31));
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenReturnCommandDateIsNotLastDayOfMonth() {
    ReturnCommand command = new ReturnCommand();
    command.setCustomPed(LocalDate.of(2025, 1, 15));
    command.setCurrency(Currency.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("CPED_NOT_MONTH_END");
        });
  }
}
