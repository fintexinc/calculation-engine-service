package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

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
    cmd.setCurrency(CurrencyType.CAD);

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
    cmd.setCurrency(CurrencyType.CAD);

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
    cmd.setCurrency(CurrencyType.CAD);

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenRollingPeriodsAreEmpty() {
    var cmd = new RollingCalculationCommand();
    cmd.setRollingPeriods(Collections.emptySet());
    cmd.setCurrency(CurrencyType.CAD);

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenCommandIsNotRollingCommand() {
    var cmd = new PeriodCommand();
    cmd.setCurrency(CurrencyType.CAD);

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }
}
