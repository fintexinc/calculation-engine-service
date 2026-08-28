package ca.tangerine.pce.application.returns.processor;

import ca.tangerine.pce.application.returns.PerformancePeriodCalculator;
import ca.tangerine.pce.application.returns.ProcessingCase;
import ca.tangerine.pce.application.returns.ProcessingContext;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.model.domain.calculation.returns.ReturnsData;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

/**
 * Trims each holding's returns to entries on or after the requested start date — the user's {@code cpsd} when supplied,
 * otherwise the snapshot's inferred performance start date. Produces a new snapshot with a freshly built returns map
 * and a refreshed performance window so downstream consumers do not observe a stale {@code performanceStartDate}; the
 * original snapshot is left untouched.
 */
@Component
@Order(240)
public class CutByCpsdOrPsdProcessor implements ReturnsProcessor {

  @Override
  public <T extends ReturnsData> ReturnsSnapshot<T> process(ReturnsSnapshot<T> snapshot, ProcessingContext context) {
    LocalDate effectiveStartDate = context.cpsd() != null ? context.cpsd() : snapshot.performanceStartDate();
    if (effectiveStartDate == null) {
      return snapshot;
    }
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimmed = PerformancePeriodCalculator.trimByStartDate(
        snapshot.returnsMap(), effectiveStartDate);
    return snapshot
        .withReturnsMap(trimmed)
        .withPeriod(PerformancePeriodCalculator.findPerformanceStartDate(trimmed), snapshot.performanceEndDate());
  }

  @Override
  public boolean isApplicable(ProcessingCase processingCase) {
    return processingCase != ProcessingCase.PORTFOLIO_PRE_PSD_TRIM
        && processingCase != ProcessingCase.BENCHMARK_PRE_PSD_TRIM
        && processingCase != ProcessingCase.PORTFOLIO_PER_FUND_FX_ONLY;
  }
}
