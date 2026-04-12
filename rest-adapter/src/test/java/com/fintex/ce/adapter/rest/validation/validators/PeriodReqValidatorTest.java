package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodReqValidatorTest {

  private final PeriodReqValidator validator = new PeriodReqValidator();

  static Stream<String> validPeriods() {
    return Stream.of("12", "36", "YEAR_TO_DATE", "SINCE_PERFORMANCE_START_DATE");
  }

  @ParameterizedTest
  @MethodSource("validPeriods")
  void shouldNotThrow_whenPeriodsAreValid(String period) {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of(period));
    command.setCurrency(CurrencyType.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  static Stream<String> zeroOrNegativePeriods() {
    return Stream.of("0", "-5");
  }

  @ParameterizedTest
  @MethodSource("zeroOrNegativePeriods")
  void shouldThrow_whenNumericPeriodIsZeroOrNegative(String period) {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of(period));
    command.setCurrency(CurrencyType.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThatCode(() -> rve.getCode()).doesNotThrowAnyException();
        });
  }

  @Test
  void shouldThrow_whenNonNumericPeriodIsInvalid() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of("INVALID_PERIOD"));
    command.setCurrency(CurrencyType.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ReqValidationException.class)
        .hasMessageContaining("INVALID_PERIOD");
  }

  @Test
  void shouldNotThrow_whenPeriodsAreEmpty() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Collections.emptySet());
    command.setCurrency(CurrencyType.CAD);

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenCommandIsNotPeriodCommand() {
    PortfolioHoldingsCommand command = new PortfolioHoldingsCommand();

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }
}
