package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

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
    command.setCurrency(CurrencyType.CAD);

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_RRC_TIP_002");
        });
  }

  @Test
  void shouldNotThrow_whenPeriodsDoNotContainYearToDate() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of("12", "36"));
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
