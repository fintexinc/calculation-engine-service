package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.error.exceptions.ReqValidationException;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class AbstractPeriodsNotContainingReqValidatorTest {

  abstract RequestValidator createValidator();

  abstract String disallowedPeriodName();

  abstract String expectedErrorCode();

  @Test
  void shouldThrow_whenPeriodsContainDisallowedValue() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of(disallowedPeriodName()));
    command.setCurrency(Currency.CAD);

    assertThatThrownBy(() -> createValidator().validate(command))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo(expectedErrorCode());
        });
  }

  @Test
  void shouldThrow_whenRollingPeriodsContainDisallowedValue() {
    RollingCalculationCommand command = new RollingCalculationCommand();
    command.setRollingPeriods(Set.of(disallowedPeriodName()));
    command.setCurrency(Currency.CAD);

    assertThatThrownBy(() -> createValidator().validate(command))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo(expectedErrorCode());
        });
  }

  @Test
  void shouldNotThrow_whenPeriodsContainOnlyNumericValues() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of("12", "36"));
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> createValidator().validate(command)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenPeriodsAreEmpty() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of());
    command.setCurrency(Currency.CAD);

    assertThatCode(() -> createValidator().validate(command)).doesNotThrowAnyException();
  }
}
