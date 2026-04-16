package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.error.exceptions.ReqValidationException;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RollingPeriodsReqValidatorTest {

  private final RollingPeriodsReqValidator validator = new RollingPeriodsReqValidator();

  @Test
  void shouldThrow_whenRollingPeriodIsNonNumeric() {
    var cmd = new RollingCalculationCommand();
    cmd.setRollingPeriods(Set.of("abc"));
    cmd.setCurrency(Currency.CAD);

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_RRC_TIP_004");
        });
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "-5"})
  void shouldThrow_whenRollingPeriodIsZeroOrNegative(String period) {
    var cmd = new RollingCalculationCommand();
    cmd.setRollingPeriods(Set.of(period));
    cmd.setCurrency(Currency.CAD);

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_RRC_RTIP_003");
        });
  }

  @Test
  void shouldNotThrow_whenRollingPeriodsAreValid() {
    var cmd = new RollingCalculationCommand();
    cmd.setRollingPeriods(Set.of("12", "36"));
    cmd.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenRollingPeriodsAreEmpty() {
    var cmd = new RollingCalculationCommand();
    cmd.setRollingPeriods(Collections.emptySet());
    cmd.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenCommandIsNotRollingCommand() {
    var cmd = new PeriodCommand();
    cmd.setCurrency(Currency.CAD);

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }
}
