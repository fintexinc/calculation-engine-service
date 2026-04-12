package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

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
    command.setCurrency(CurrencyType.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_RRC_TIP_001");
        });
  }

  @ParameterizedTest
  @MethodSource("periodsAtLeast12")
  void shouldNotThrow_whenNumericPeriodIsAtLeast12(String period) {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of(period));
    command.setCurrency(CurrencyType.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenPeriodsAreNonNumeric() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of("YEAR_TO_DATE"));
    command.setCurrency(CurrencyType.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenPeriodsAreEmpty() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of());
    command.setCurrency(CurrencyType.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }
}
