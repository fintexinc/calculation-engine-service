package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StandardDeviationPeriodsReqValidatorTest {

  private final StandardDeviationPeriodsReqValidator validator = new StandardDeviationPeriodsReqValidator();

  @Test
  void shouldThrowException_whenPeriodsContainYearToDate() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of(TimePeriod.YTD));

    assertThatThrownBy(() -> validator.validate(command))
        .isInstanceOfSatisfying(ValidationException.class, exception -> assertThat(exception.getErrorCode()).isEqualTo(
            ErrorCode.TIME_INTERVAL_PERIOD_NOT_SUPPORTED));
  }

  @Test
  void shouldAllowShortPeriods_whenStandardDeviationRequiresNullResult() {
    PeriodCommand command = new PeriodCommand();
    command.setPeriods(Set.of(TimePeriod.SIX_MTH));

    assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
  }
}
