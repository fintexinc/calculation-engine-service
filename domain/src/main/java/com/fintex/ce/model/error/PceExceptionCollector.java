package com.fintex.ce.model.error;

import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Aggregates {@link BasePceException} instances (both {@code CalculationException} and {@code ValidationException})
 * accumulated across one or more calculation or validation steps. Consumers add exceptions as they are detected and
 * invoke {@link #throwIfAny()} (or a filtered variant) at a safe point to surface them as a single
 * {@link CalculationsFailedException} to the REST boundary.
 */
@Getter
@EqualsAndHashCode
public class PceExceptionCollector {

  private final List<BasePceException> exceptions = new ArrayList<>();

  public void add(BasePceException exception) {
    exceptions.add(exception);
  }

  public void addAll(Collection<? extends BasePceException> toAdd) {
    exceptions.addAll(toAdd);
  }

  public boolean hasErrors() {
    return !exceptions.isEmpty();
  }

  public <T> T tryCatch(Supplier<T> supplier) {
    try {
      return supplier.get();
    } catch (CalculationsFailedException e) {
      exceptions.addAll(e.getExceptions());
      return null;
    } catch (BasePceException e) {
      exceptions.add(e);
      return null;
    }
  }

  public void throwIfAny() {
    if (hasErrors()) {
      throw new CalculationsFailedException(exceptions);
    }
  }

  public void throwIfAnyNonAllowed(List<ErrorCode> allowedErrors) {
    if (hasErrors() && exceptions.stream().anyMatch(e -> !allowedErrors.contains(e.getErrorCode()))) {
      throw new CalculationsFailedException(exceptions);
    }
  }
}
