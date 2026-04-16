package com.fintex.ce.model.error;

import com.fintex.ce.model.error.exceptions.DataErrorException;
import com.fintex.ce.model.error.exceptions.FdsDataValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.Data;

/**
 * A Notification collect together errors
 */
@Data
public class Notification {

  List<DataErrorException> errors = new ArrayList<>();

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public void addError(DataErrorException e) {
    errors.add(e);
  }

  public void addErrors(List<DataErrorException> errors) {
    this.errors.addAll(errors);
  }

  public <T> T tryCatch(Supplier<T> supplier) {
    try {
      return supplier.get();
    } catch (FdsDataValidationException e) {
      e.getExceptionList().forEach(this::addError);
      return null;
    }
  }

  public void ifAnyErrorThrowException() {
    if (hasErrors()) {
      throw new FdsDataValidationException(getErrors());
    }
  }

  public void ifAnyNonAllowedErrorThrowException(List<ErrorCode> allowedErrors) {
    if (hasErrors() && hasNonAllowedErrors(allowedErrors)) {
      throw new FdsDataValidationException(getErrors());
    }
  }

  private boolean hasNonAllowedErrors(List<ErrorCode> allowedErrors) {
    return errors.stream().anyMatch(error -> !allowedErrors.contains(error.getCode()));
  }
}
