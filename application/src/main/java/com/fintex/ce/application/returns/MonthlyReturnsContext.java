package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.calculation.returns.ReturnsData;

/**
 * Bundle of the snapshot, FX inputs, and role produced by the build phase of {@code MonthlyReturnsService}. Passed into
 * pipeline-execution methods (weighted-average, alignment) so the orchestrator can pick the correct
 * {@link ProcessingCase} without further configuration calls.
 */
public record MonthlyReturnsContext<T extends ReturnsData>(
    ReturnsSnapshot<T> snapshot,
    FxContext fxContext,
    ReturnsRole role) {

  public MonthlyReturnsContext<T> withSnapshot(ReturnsSnapshot<T> updatedSnapshot) {
    return new MonthlyReturnsContext<>(updatedSnapshot, fxContext, role);
  }
}
