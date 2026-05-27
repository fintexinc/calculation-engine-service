package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.MonthlyReturnsGenerator;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.util.SecurityDataValidator;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

/**
 * Application-layer entry point for monthly-returns data sourcing. Fetches per-holding monthly returns from Security
 * Master, merges in locally-synthesized GIC returns, validates the result, and wraps it in a {@link ReturnsSnapshot}.
 *
 * <p>
 * Role-tagged context construction (snapshot + FX + role) is delegated to
 * {@link com.fintex.ce.application.returns.MonthlyReturnsContextProvider} subclasses
 * ({@link com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider},
 * {@link com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider}). Callers inject the specific
 * provider for the side they need. Per-case pipeline execution lives in
 * {@link com.fintex.ce.application.returns.pipeline.MonthlyReturnsPipeline} subclasses; context manipulation
 * (trim-to-end, common-end-date) lives on {@link com.fintex.ce.application.returns.MonthlyReturnsContext} and
 * {@link ReturnsSnapshot} themselves.
 */
@Slf4j
@Service
public class MonthlyReturnsService {

  private final SecurityDataFetcher<HoldingMonthlyReturns> monthlyReturnsSecurityDataFetcher;
  private final MonthlyReturnsGenerator monthlyReturnsGenerator;

  public MonthlyReturnsService(SecurityDataFetcher<HoldingMonthlyReturns> monthlyReturnsSecurityDataFetcher,
      MonthlyReturnsGenerator monthlyReturnsGenerator) {
    this.monthlyReturnsSecurityDataFetcher = monthlyReturnsSecurityDataFetcher;
    this.monthlyReturnsGenerator = monthlyReturnsGenerator;
  }

  public ReturnsSnapshot<HoldingMonthlyReturns> getMonthlyReturns(List<PortfolioHolding> holdings) {
    Map<PortfolioHolding, HoldingMonthlyReturns> sourceData = new HashMap<>(
        monthlyReturnsSecurityDataFetcher.fetch(holdings, List.of()));
    sourceData.putAll(monthlyReturnsGenerator.generateGicMonthlyReturns(holdings));
    validateMonthlyReturnsPresent(holdings, sourceData);
    return ReturnsSnapshot.forMonthlyReturns(sourceData);
  }

  public ReturnsSnapshot<HoldingMonthlyReturns> getMonthlyReturnsOnlyWithMonthlyReturnsDataValidation(
      List<PortfolioHolding> holdings) {
    Map<PortfolioHolding, HoldingMonthlyReturns> sourceData = monthlyReturnsSecurityDataFetcher.fetch(holdings,
        List.of());
    return ReturnsSnapshot.validateOnly(sourceData);
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
   * returns are synthesized locally by {@code MonthlyReturnsGenerator}; CASH carries no returns and gets zero weight
   * downstream.
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
  }

  private boolean isReturnsEmpty(HoldingMonthlyReturns holdingReturns) {
    return CollectionUtils.isEmpty(holdingReturns.getReturns());
  }
}
