package com.fintex.ce.application.returns.processor;

import com.fintex.ce.application.returns.PerformancePeriodCalculator;
import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.application.returns.ProcessingContext;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.model.domain.calculation.returns.ReturnsData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

/**
 * Trims each holding's returns to entries on or before the earlier of the user's {@code cped} and the snapshot's
 * inferred performance end date. Produces a new snapshot with a freshly built returns map and a refreshed performance
 * window so downstream consumers do not observe a stale {@code performanceEndDate}; the original snapshot is left
 * untouched.
 */
@Component
@Order(200)
public class CutByCpedOrPedProcessor implements ReturnsProcessor {

  @Override
  public <T extends ReturnsData> ReturnsSnapshot<T> process(ReturnsSnapshot<T> snapshot, ProcessingContext context) {
    LocalDate requestedEndDate = context.cped();
    LocalDate availableEndDate = snapshot.performanceEndDate();
    LocalDate effectiveEndDate = requestedEndDate == null
        || availableEndDate != null && availableEndDate.isBefore(requestedEndDate)
            ? availableEndDate
            : requestedEndDate;
    if (effectiveEndDate == null) {
      return snapshot;
    }
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimmed = PerformancePeriodCalculator.trimByEndDate(
        snapshot.returnsMap(), effectiveEndDate);
    return snapshot
        .withReturnsMap(trimmed)
        .withPeriod(snapshot.performanceStartDate(), PerformancePeriodCalculator.findPerformanceEndDate(trimmed));
  }

  @Override
  public boolean isApplicable(ProcessingCase processingCase) {
    return processingCase != ProcessingCase.PORTFOLIO_PER_FUND_FX_ONLY;
  }
}
