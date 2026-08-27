package ca.tangerine.pce.application.returns;

import java.time.LocalDate;

/**
 * Per-invocation parameters threaded through every {@code ReturnsProcessor} call in a pipeline.
 *
 * <p>
 * Holds the user-supplied custom dates ({@code cpsd}, {@code cped}) and the FX inputs for the current request. Fields
 * may be {@code null} when not supplied — processors that need a value are responsible for falling back to defaults
 * derived from the snapshot (typically {@code performanceStartDate} or {@code performanceEndDate}).
 * </p>
 */
public record ProcessingContext(
    LocalDate cpsd,
    LocalDate cped,
    FxContext fx) {

  public ProcessingContext {
    fx = fx == null ? FxContext.empty() : fx;
  }

  public static ProcessingContext of(LocalDate cpsd, LocalDate cped, FxContext fx) {
    return new ProcessingContext(cpsd, cped, fx);
  }
}
