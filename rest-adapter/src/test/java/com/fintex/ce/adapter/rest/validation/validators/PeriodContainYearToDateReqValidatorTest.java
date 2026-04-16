package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.exceptions.ReqValidationException;
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
