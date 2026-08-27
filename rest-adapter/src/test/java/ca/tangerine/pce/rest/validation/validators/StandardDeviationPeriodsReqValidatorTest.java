package ca.tangerine.pce.rest.validation.validators;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.ValidationException;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

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
