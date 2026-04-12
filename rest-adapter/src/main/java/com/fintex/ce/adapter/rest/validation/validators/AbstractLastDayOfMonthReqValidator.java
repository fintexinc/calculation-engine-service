package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.exception.code.ErrorCode;

import java.time.LocalDate;
import java.util.function.Function;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;

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
      throw errorCode.reqValidationError();
    }
  }
}
