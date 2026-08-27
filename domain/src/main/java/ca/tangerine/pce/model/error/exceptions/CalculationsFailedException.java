package ca.tangerine.pce.model.error.exceptions;

import java.io.Serial;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

import ca.tangerine.pce.model.error.PceExceptionCollector;

/**
 * Aggregate runtime exception carrying a list of {@link BasePceException} instances collected during one or more
 * calculation or validation steps. Thrown from {@link PceExceptionCollector#throwIfAny()}.
 */
@Getter
public class CalculationsFailedException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final List<BasePceException> exceptions;

  public CalculationsFailedException(List<? extends BasePceException> exceptions) {
    super(exceptions.isEmpty()
        ? "Calculation failed"
        : "Calculation failed due to the errors: "
            + exceptions.stream().map(Throwable::getMessage).collect(Collectors.joining("; ")));
    this.exceptions = List.copyOf(exceptions);
  }
}
