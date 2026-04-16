package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.error.ErrorCode;

import java.time.LocalDate;
import java.util.function.Function;

public abstract class AbstractDateNotAfterReqValidator implements RequestValidator {

  private final Function<CalculationCommand, LocalDate> firstDateAccessor;
  private final Function<CalculationCommand, LocalDate> secondDateAccessor;
  private final ErrorCode errorCode;

  protected AbstractDateNotAfterReqValidator(
      Function<CalculationCommand, LocalDate> firstDateAccessor,
      Function<CalculationCommand, LocalDate> secondDateAccessor,
      ErrorCode errorCode) {
    this.firstDateAccessor = firstDateAccessor;
    this.secondDateAccessor = secondDateAccessor;
    this.errorCode = errorCode;
  }

  @Override
  public void validate(CalculationCommand command) {
    LocalDate first = firstDateAccessor.apply(command);
    LocalDate second = secondDateAccessor.apply(command);
    if (first != null && second != null && first.isAfter(second)) {
      throw errorCode.reqValidationError();
    }
  }
}
