package ca.tangerine.pce.rest.validation.validators;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.error.exceptions.ValidationException;
import ca.tangerine.wm.commons.domain.currency.Currency;

class CipsdGreaterThanCpedReqValidatorTest {

  private final CipsdGreaterThanCpedReqValidator validator = new CipsdGreaterThanCpedReqValidator();

  @Test
  void shouldThrow_whenCipsdAfterCped() {
    PeriodCommand command = new PeriodCommand();
    command.setCustomIntervalPsd(LocalDate.of(2025, 6, 30));
    command.setCustomPed(LocalDate.of(2025, 1, 31));
    command.setCurrency(Currency.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("CIPSD_AFTER_CPED");
        });
  }

  @Test
  void shouldNotThrow_whenCipsdBeforeCped() {
    PeriodCommand command = new PeriodCommand();
    command.setCustomIntervalPsd(LocalDate.of(2025, 1, 31));
    command.setCustomPed(LocalDate.of(2025, 6, 30));
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenDatesAreNull() {
    PeriodCommand command = new PeriodCommand();
    command.setCustomIntervalPsd(null);
    command.setCustomPed(null);
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenOnlyOneDateSet() {
    PeriodCommand commandWithCipsdOnly = new PeriodCommand();
    commandWithCipsdOnly.setCustomIntervalPsd(LocalDate.of(2025, 6, 30));
    commandWithCipsdOnly.setCustomPed(null);
    commandWithCipsdOnly.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(commandWithCipsdOnly)).doesNotThrowAnyException();

    PeriodCommand commandWithCpedOnly = new PeriodCommand();
    commandWithCpedOnly.setCustomIntervalPsd(null);
    commandWithCpedOnly.setCustomPed(LocalDate.of(2025, 6, 30));
    commandWithCpedOnly.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(commandWithCpedOnly)).doesNotThrowAnyException();
  }
}
