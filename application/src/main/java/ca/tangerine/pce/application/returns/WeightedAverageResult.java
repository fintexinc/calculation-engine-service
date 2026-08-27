package ca.tangerine.pce.application.returns;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;

import ca.tangerine.pce.model.domain.calculation.returns.ReturnsData;
import ca.tangerine.wm.commons.error.Notification;

/**
 * Output of a weighted-average pipeline run. Bundles the computed time series with the post-pipeline snapshot so
 * callers can read both the values and any warnings accumulated by validators or processors.
 */
public record WeightedAverageResult<T extends ReturnsData>(
    NavigableMap<LocalDate, BigDecimal> weightedAverage,
    ReturnsSnapshot<T> snapshot) implements PipelineResult<T> {

  public List<Notification> getErrorsAsWarnings() {
    return snapshot.getErrorsAsWarnings();
  }
}
