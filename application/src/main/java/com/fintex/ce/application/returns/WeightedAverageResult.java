package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.calculation.returns.ReturnsData;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;

/**
 * Output of a weighted-average pipeline run. Bundles the computed time series with the post-pipeline snapshot so
 * callers can read both the values and any warnings accumulated by validators or processors.
 */
public record WeightedAverageResult<T extends ReturnsData>(
    NavigableMap<LocalDate, BigDecimal> weightedAverage,
    ReturnsSnapshot<T> snapshot) {

  public List<Notification> getErrorsAsWarnings() {
    return snapshot.getErrorsAsWarnings();
  }
}
