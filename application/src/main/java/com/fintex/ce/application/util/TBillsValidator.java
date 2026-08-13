package com.fintex.ce.application.util;

import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import lombok.experimental.UtilityClass;

import static com.fintex.ce.application.util.ReturnSeriesAlignmentValidator.formatDates;

/**
 * Pre-condition checks for T-Bill series consumed by metric services. The {@code TreasuryBillsFetcher} pre-populates
 * every {@link Currency} with an empty {@link NavigableMap} so callers don't NPE on lookup, but several metrics
 * (Sortino, DownsideDeviation, Alpha, Beta, R-Squared, Treynor, RollingSharpe) compose the T-Bill series into derived
 * inputs (excess returns, risk-free rate). Letting an empty series flow through silently collapses
 * {@code availableMonths()} to 0 and surfaces a misleading {@code RET-008} ("only 0 monthly returns available") that
 * misattributes the cause to portfolio data. This helper fails fast with
 * {@link ErrorCode#TBILL_SERIES_NOT_AVAILABLE_FOR_CURRENCY} (HTTP 400), which is distinct from the per-date
 * {@link ErrorCode#MISSING_TBILL_RATE} that {@code RiskFreeWindowValidator.requireCoverage} uses for partial coverage
 * gaps.
 */
@UtilityClass
public final class TBillsValidator {

  public static NavigableMap<LocalDate, BigDecimal> requireNonEmpty(
      NavigableMap<LocalDate, BigDecimal> tBills, Currency currency) {
    if (tBills == null || tBills.isEmpty()) {
      throw new CalculationException(ErrorCode.TBILL_SERIES_NOT_AVAILABLE_FOR_CURRENCY, currency);
    }
    return tBills;
  }

  public static NavigableMap<LocalDate, BigDecimal> requireCompleteCalendarMonths(
      NavigableMap<LocalDate, BigDecimal> tBills, Currency currency) {
    NavigableMap<LocalDate, BigDecimal> validatedTBills = requireNonEmpty(tBills, currency);
    List<LocalDate> missingDates = ReturnSeriesAlignmentValidator.findMissingCalendarMonthEnds(validatedTBills);
    if (!missingDates.isEmpty()) {
      throw ErrorCode.MISSING_TBILL_RATE.toException(formatDates(missingDates));
    }
    return validatedTBills;
  }
}