package ca.tangerine.pce.rest.validation.validators;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ca.tangerine.pce.model.dto.command.ReturnCommand;
import ca.tangerine.pce.model.error.exceptions.ValidationException;
import ca.tangerine.wm.commons.domain.currency.Currency;

class CpsdGreaterThanCpedReqValidatorTest {

  private final CpsdGreaterThanCpedReqValidator validator = new CpsdGreaterThanCpedReqValidator();

  @Test
  void shouldThrow_whenCustomPsdIsAfterCustomPed() {
    ReturnCommand command = new ReturnCommand();
    command.setCustomPsd(LocalDate.of(2025, 6, 30));
    command.setCustomPed(LocalDate.of(2025, 1, 31));
    command.setCurrency(Currency.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("CPSD_AFTER_CPED");
          assertThat(rve.getMessage()).contains("Custom Performance Start Date");
        });
  }

  @Test
  void shouldThrow_whenCustomPsdIsAfterCustomPed_forReturnCommand() {
    ReturnCommand command = new ReturnCommand();
    command.setCustomPsd(LocalDate.of(2025, 12, 31));
    command.setCustomPed(LocalDate.of(2025, 3, 31));
    command.setCurrency(Currency.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("CPSD_AFTER_CPED");
        });
  }

  @Test
  void shouldNotThrow_whenCustomPsdIsBeforeCustomPed() {
    ReturnCommand command = new ReturnCommand();
    command.setCustomPsd(LocalDate.of(2025, 1, 31));
    command.setCustomPed(LocalDate.of(2025, 6, 30));
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenDatesAreNull() {
    ReturnCommand command = new ReturnCommand();
    command.setCustomPsd(null);
    command.setCustomPed(null);
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenOnlyOneDateIsSet() {
    ReturnCommand commandWithPsdOnly = new ReturnCommand();
    commandWithPsdOnly.setCustomPsd(LocalDate.of(2025, 6, 30));
    commandWithPsdOnly.setCustomPed(null);
    commandWithPsdOnly.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(commandWithPsdOnly)).doesNotThrowAnyException();

    ReturnCommand commandWithPedOnly = new ReturnCommand();
    commandWithPedOnly.setCustomPsd(null);
    commandWithPedOnly.setCustomPed(LocalDate.of(2025, 6, 30));
    commandWithPedOnly.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(commandWithPedOnly)).doesNotThrowAnyException();
  }
}
