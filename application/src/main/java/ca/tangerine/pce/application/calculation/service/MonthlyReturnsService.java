package ca.tangerine.pce.application.calculation.service;

import ca.tangerine.pce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import ca.tangerine.pce.application.returns.CashMonthlyReturnsGenerator;
import ca.tangerine.pce.application.returns.MonthlyReturnsContext;
import ca.tangerine.pce.application.returns.MonthlyReturnsContextProvider;
import ca.tangerine.pce.application.returns.MonthlyReturnsGenerator;
import ca.tangerine.pce.application.returns.PortfolioMonthlyReturnsContextProvider;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.application.returns.pipeline.MonthlyReturnsPipeline;
import ca.tangerine.pce.application.util.SecurityDataValidator;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.util.FilterUtils;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static ca.tangerine.pce.application.util.ReturnSeriesAlignmentValidator.findMissingCalendarMonthEnds;
import static java.util.stream.Collectors.joining;

/**
 * Application-layer entry point for monthly-returns data sourcing. Consumes the pre-fetched monthly-returns map
 * supplied by the orchestrator, restricts it to the requested holdings (the map may cover a superset, e.g. portfolio
 * plus benchmark), merges in the locally-sourced CASH ({@link CashMonthlyReturnsGenerator}, T-Bill based) and GIC
 * ({@link MonthlyReturnsGenerator}, interest-rate based) returns, validates the result, and wraps it in a
 * {@link ReturnsSnapshot}.
 *
 * <p>
 * Role-tagged context construction (snapshot + FX + role) is delegated to {@link MonthlyReturnsContextProvider}
 * subclasses ({@link PortfolioMonthlyReturnsContextProvider}, {@link BenchmarkMonthlyReturnsContextProvider}). Callers
 * inject the specific provider for the side they need. Per-case pipeline execution lives in
 * {@link MonthlyReturnsPipeline} subclasses; context manipulation (trim-to-end, common-end-date) lives on
 * {@link MonthlyReturnsContext} and {@link ReturnsSnapshot} themselves.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyReturnsService {

  private final MonthlyReturnsGenerator monthlyReturnsGenerator;
  private final CashMonthlyReturnsGenerator cashMonthlyReturnsGenerator;

  public ReturnsSnapshot<HoldingMonthlyReturns> getMonthlyReturns(List<PortfolioHolding> holdings,
      Map<PortfolioHolding, HoldingMonthlyReturns> monthlyReturns) {
    Map<PortfolioHolding, HoldingMonthlyReturns> sourceData = FilterUtils.restrictToHoldings(monthlyReturns,
        holdings);
    sourceData.putAll(monthlyReturnsGenerator.generateGicMonthlyReturns(holdings));
    sourceData.putAll(cashMonthlyReturnsGenerator.generateCashMonthlyReturns(holdings));
    validateMonthlyReturnsPresent(holdings, sourceData);
    return ReturnsSnapshot.forMonthlyReturns(sourceData);
  }

  public ReturnsSnapshot<HoldingMonthlyReturns> getMonthlyReturnsOnlyWithReturnsValidation(
      List<PortfolioHolding> holdings, Map<PortfolioHolding, HoldingMonthlyReturns> monthlyReturns) {
    return ReturnsSnapshot.validateOnly(FilterUtils.restrictToHoldings(monthlyReturns, holdings));
  }

  /**
   * Per spec, every holding must have monthly returns for the calculation to proceed. Two failure modes:
   * <ul>
   * <li>The data source returned no row at all for a holding the caller asked about → delegated to
   * {@link SecurityDataValidator#requireDataForEveryHolding}, which throws
   * {@link ErrorCode#NO_SECURITY_DATA_FOR_HOLDING}. Identifier-based, so a portfolio with two holdings sharing the same
   * ticker (e.g. same fund in two accounts) passes when the source returns the single deduped row.</li>
   * <li>The data source returned a row but its monthly-returns list is empty →
   * {@link ErrorCode#MISSING_MONTHLY_RETURNS}.</li>
   * </ul>
   * Holdings of types in {@link FilterUtils#LOCALLY_SOURCED_TYPES} (CASH, GIC) are excluded from both checks: GIC
   * returns are synthesized locally by {@code MonthlyReturnsGenerator}; CASH returns are sourced from currency-specific
   * Treasury Bill series.
   */
  private void validateMonthlyReturnsPresent(List<PortfolioHolding> holdings,
      Map<PortfolioHolding, HoldingMonthlyReturns> sourceData) {
    Predicate<PortfolioHolding> mandatoryForExternalSource = holding -> {
      FinancialInstrumentType type = holding.getHoldingType();
      return type == null || !FilterUtils.LOCALLY_SOURCED_TYPES.contains(type);
    };
    SecurityDataValidator.requireDataForEveryHolding(sourceData, holdings, mandatoryForExternalSource);
    sourceData.entrySet().stream()
        .filter(entry -> mandatoryForExternalSource.test(entry.getKey()) && isReturnsEmpty(entry.getValue()))
        .findFirst()
        .ifPresent(entry -> {
          throw ErrorCode.MISSING_MONTHLY_RETURNS.toExceptionForHolding(entry.getKey());
        });
    sourceData.entrySet().stream()
        .filter(entry -> mandatoryForExternalSource.test(entry.getKey()))
        .map(entry -> Map.entry(entry.getKey(), findMissingCalendarMonthEnds(entry.getValue().getReturns())))
        .filter(entry -> !entry.getValue().isEmpty())
        .findFirst()
        .ifPresent(entry -> {
          String missingDates = entry.getValue().stream().map(Object::toString).collect(joining(", "));
          throw ErrorCode.MISSING_MONTHLY_RETURN_FOR_DATE.toExceptionForHolding(entry.getKey(), missingDates);
        });
  }

  private boolean isReturnsEmpty(HoldingMonthlyReturns holdingReturns) {
    return CollectionUtils.isEmpty(holdingReturns.getReturns());
  }
}
