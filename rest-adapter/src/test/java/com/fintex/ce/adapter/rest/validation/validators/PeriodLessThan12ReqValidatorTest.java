package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodLessThan12ReqValidatorTest {

  private final PeriodLessThan12ReqValidator validator = new PeriodLessThan12ReqValidator();

  static Stream<String> periodsLessThan12() {
    return Stream.of("1", "6", "11");
  }

  static Stream<String> periodsAtLeast12() {
    return Stream.of("12", "36");
  }

  @ParameterizedTest
  @MethodSource("periodsLessThan12")
  void shouldThrow_whenNumericPeriodLessThan12(String period) {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of(period));
    command.setCurrency(Currency.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode().name()).isEqualTo("TIME_INTERVAL_PERIOD_LESS_THAN_12");
        });
  }

  @ParameterizedTest
  @MethodSource("periodsAtLeast12")
  void shouldNotThrow_whenNumericPeriodIsAtLeast12(String period) {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of(period));
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenPeriodsAreNonNumeric() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of("YEAR_TO_DATE"));
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
