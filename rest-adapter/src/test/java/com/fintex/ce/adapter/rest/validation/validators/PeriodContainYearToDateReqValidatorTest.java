package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodContainYearToDateReqValidatorTest {

  private final PeriodContainYearToDateReqValidator validator = new PeriodContainYearToDateReqValidator();

  @Test
  void shouldThrow_whenPeriodsContainYearToDate() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of("YEAR_TO_DATE"));
    command.setCurrency(Currency.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("TIME_INTERVAL_PERIOD_CONTAINS_YEAR_TO_DATE");
        });
  }

  @Test
  void shouldNotThrow_whenPeriodsDoNotContainYearToDate() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of("12", "36"));
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenPeriodsAreEmpty() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of());
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }
}
