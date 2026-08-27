package ca.tangerine.pce.application.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.SortedMap;
import lombok.experimental.UtilityClass;

import ca.tangerine.pce.model.error.ErrorCode;

/**
 * Single source of truth for the per-date T-Bill coverage precondition. Throws {@link ErrorCode#MISSING_TBILL_RATE}
 * (request-terminating HTTP 400) for the first date in the requested window that has no entry in the risk-free series.
 * {@code riskFreeDerivedSeries} is either the T-Bill series directly (Sharpe, Sortino, Treynor, Trailing Total Returns)
 * or a T-Bill-derived excess-return series (DownsideDeviation, Alpha, Beta, R-Squared) — in both cases the upstream
 * cause of any gap is a missing T-Bill rate.
 *
 * <p>
 * The throw is intentional and load-bearing: {@code RollingSharpeRatioCalculation} catches it to degrade a single
 * rolling window to {@code null} without failing the whole request.
 */
@UtilityClass
public final class RiskFreeWindowValidator {

  public static void requireCoverage(SortedMap<LocalDate, BigDecimal> window,
      NavigableMap<LocalDate, BigDecimal> riskFreeDerivedSeries) {
    window.keySet().stream()
        .filter(date -> !riskFreeDerivedSeries.containsKey(date))
        .findFirst()
        .ifPresent(date -> {
          throw ErrorCode.MISSING_TBILL_RATE.toException(date);
        });
  }
}
