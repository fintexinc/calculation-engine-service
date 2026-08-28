package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.rest.validation.RequestValidator;

import java.time.LocalDate;
import java.util.function.Function;

import static ca.tangerine.pce.util.DateTimeUtils.toLastDayOfMonth;

/**
 * Base validator that verifies a single {@link LocalDate} extracted from the command matches the last day of its month.
 * Subclasses provide the accessor, the carrier type to apply the rule to, and the {@link ErrorCode} to throw on
 * violation.
 */
public abstract class AbstractLastDayOfMonthReqValidator<T> implements RequestValidator {

  private final Class<T> carrierType;
  private final Function<T, LocalDate> dateAccessor;
  private final ErrorCode errorCode;

  protected AbstractLastDayOfMonthReqValidator(Class<T> carrierType,
      Function<T, LocalDate> dateAccessor,
      ErrorCode errorCode) {
    this.carrierType = carrierType;
    this.dateAccessor = dateAccessor;
    this.errorCode = errorCode;
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!carrierType.isInstance(command)) {
      return;
    }
    LocalDate date = dateAccessor.apply(carrierType.cast(command));
    if (date != null && !date.equals(toLastDayOfMonth(date))) {
      throw errorCode.toValidationException();
    }
  }
}
