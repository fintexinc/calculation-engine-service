package ca.tangerine.pce.rest.validation.validators;

import java.time.LocalDate;
import java.util.function.Function;

import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.rest.validation.RequestValidator;

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
      throw errorCode.toValidationException();
    }
  }
}
