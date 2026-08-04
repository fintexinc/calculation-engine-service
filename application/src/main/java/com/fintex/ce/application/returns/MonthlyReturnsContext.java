package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.calculation.returns.ReturnsData;

import java.time.LocalDate;

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

  /**
   * Returns this context with its snapshot trimmed at {@code endDate}. No-op when {@code endDate} is null or already
   * matches the current PED.
   */
  public MonthlyReturnsContext<T> trimToEnd(LocalDate endDate) {
    return withSnapshot(snapshot.trimToEnd(endDate));
  }

  /** Returns this context with its snapshot trimmed to the supplied performance window. */
  public MonthlyReturnsContext<T> trimToRange(LocalDate startDate, LocalDate endDate) {
    return withSnapshot(snapshot.trimToRange(startDate, endDate));
  }

  /**
   * Later-of the two contexts' performance-start dates. Null-tolerant: if one side has no PSD, returns the other.
   */
  public LocalDate commonPerformanceStartDate(MonthlyReturnsContext<T> other) {
    LocalDate first = snapshot.performanceStartDate();
    LocalDate second = other.snapshot().performanceStartDate();
    if (first == null) {
      return second;
    }
    if (second == null) {
      return first;
    }
    return first.isAfter(second) ? first : second;
  }

  /**
   * Earlier-of the two contexts' performance-end dates. Used to align portfolio and benchmark series before weighting.
   * Null-tolerant: if one side has no PED, returns the other.
   */
  public LocalDate commonPerformanceEndDate(MonthlyReturnsContext<T> other) {
    LocalDate first = snapshot.performanceEndDate();
    LocalDate second = other.snapshot().performanceEndDate();
    if (first == null) {
      return second;
    }
    if (second == null) {
      return first;
    }
    return first.isBefore(second) ? first : second;
  }

  /** Returns whether the requested end date falls after this context's available performance end date. */
  public boolean isCustomPedAfterPerformanceEndDate(LocalDate customPed) {
    LocalDate performanceEndDate = snapshot.performanceEndDate();
    return customPed != null && performanceEndDate != null && customPed.isAfter(performanceEndDate);
  }
}
