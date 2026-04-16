package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.error.exceptions.ReqValidationException;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpsdGreaterThanCpedReqValidatorTest {

  private final CpsdGreaterThanCpedReqValidator validator = new CpsdGreaterThanCpedReqValidator();

  @Test
  void shouldThrow_whenCustomPsdIsAfterCustomPed() {
    RollingCalculationCommand command = new RollingCalculationCommand();
    command.setCustomPsd(LocalDate.of(2025, 6, 30));
    command.setCustomPed(LocalDate.of(2025, 1, 31));
    command.setCurrency(Currency.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_RRC_CPSD_004");
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
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_RRC_CPSD_004");
        });
  }

  @Test
  void shouldNotThrow_whenCustomPsdIsBeforeCustomPed() {
    RollingCalculationCommand command = new RollingCalculationCommand();
    command.setCustomPsd(LocalDate.of(2025, 1, 31));
    command.setCustomPed(LocalDate.of(2025, 6, 30));
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenDatesAreNull() {
    RollingCalculationCommand command = new RollingCalculationCommand();
    command.setCustomPsd(null);
    command.setCustomPed(null);
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenOnlyOneDateIsSet() {
    RollingCalculationCommand commandWithPsdOnly = new RollingCalculationCommand();
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
