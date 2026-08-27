package ca.tangerine.pce.application.returns.processor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import ca.tangerine.pce.application.returns.PerformancePeriodCalculator;
import ca.tangerine.pce.application.returns.ProcessingCase;
import ca.tangerine.pce.application.returns.ProcessingContext;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.model.domain.calculation.returns.ReturnsData;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;

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
