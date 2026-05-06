package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.MonthlyReturnsGenerator;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.returns.ReturnsCutComponent;
import com.fintex.ce.application.returns.WeightedAverageComponent;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.validation.BenchmarkCpedDataValidation;
import com.fintex.ce.application.validation.BenchmarkCpsdDataValidation;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MonthlyReturnsService {

  private final SecurityDataFetcher<HoldingMonthlyReturns> monthlyReturnsSecurityDataFetcher;
  private final FxRateService fxRateService;
  private final MonthlyReturnsGenerator monthlyReturnsGenerator;

  public MonthlyReturnsService(SecurityDataFetcher<HoldingMonthlyReturns> monthlyReturnsSecurityDataFetcher,
      FxRateService fxRateService,
      MonthlyReturnsGenerator monthlyReturnsGenerator) {
    this.monthlyReturnsSecurityDataFetcher = monthlyReturnsSecurityDataFetcher;
    this.fxRateService = fxRateService;
    this.monthlyReturnsGenerator = monthlyReturnsGenerator;
  }

  public NavigableMap<LocalDate, BigDecimal> getWeightedAverageWithCpsdAndCpedValidation(
      ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregate,
      LocalDate cpsd, LocalDate cped) {
    return monthlyReturnsAggregate
        .validateCped(cped)
        .validateCpsd(cpsd)
        .cutByCpedIfCpedEmptyCutByPed(cped)
        .cutByCpsdIfCpsdEmptyCutByPsd(cpsd)
        .fxRatesApplied()
        .getWeightedAverage();
  }

  public NavigableMap<LocalDate, BigDecimal> getWeightedAverageWithCpedValidation(
      ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregate,
      LocalDate cped) {
    return monthlyReturnsAggregate
        .validateCped(cped)
        .cutByCpedIfCpedEmptyCutByPed(cped)
        .cutByPsd()
        .fxRatesApplied()
        .getWeightedAverage();
  }

  public ReturnsAggregate<HoldingMonthlyReturns> getMonthlyReturns(List<PortfolioHolding> holdings, Currency currency) {
    Map<PortfolioHolding, HoldingMonthlyReturns> originalMonthlyReturns = monthlyReturnsSecurityDataFetcher.fetch(
        holdings, List
            .of());
    originalMonthlyReturns.putAll(monthlyReturnsGenerator.generateGicMonthlyReturns(holdings));
    validateMonthlyReturnsPresent(holdings, originalMonthlyReturns);
    return getMonthlyReturns(originalMonthlyReturns);
  }

  /**
   * Per spec, every holding must have monthly returns for the calculation to proceed. Distinguishes two failure modes:
   * <ul>
   * <li>holding entirely absent from the Security Master response → {@link ErrorCode#SECURITY_NOT_FOUND_IN_SM} (the
   * security identifier is unknown to the data provider)</li>
   * <li>holding present but with no monthly returns → {@link ErrorCode#MISSING_MONTHLY_RETURNS} (security exists but
   * its return history is empty)</li>
   * </ul>
   * Holdings of types in {@link FilterUtils#NOT_SENT_TO_SM_TYPES} (CASH, GIC) are excluded from both checks: they are
   * intentionally never sent to Security Master, so finding them missing from the response is expected. GIC entries
   * arrive via {@code MonthlyReturnsGenerator}; CASH carries no returns and gets zero weight downstream. Throws on the
   * first offending holding so the response carries its identifier.
   */
  private void validateMonthlyReturnsPresent(List<PortfolioHolding> holdings,
      Map<PortfolioHolding, HoldingMonthlyReturns> originalMonthlyReturns) {
    List<PortfolioHolding> sentToSm = holdings.stream()
        .filter(holding -> {
          FinancialInstrumentType type = holding.getHoldingType();
          // Treat null type as "validate" so existing not-found checks still fire for malformed holdings.
          return type == null || !FilterUtils.NOT_SENT_TO_SM_TYPES.contains(type);
        })
        .toList();
    sentToSm.stream()
        .filter(holding -> !originalMonthlyReturns.containsKey(holding))
        .findFirst()
        .ifPresent(holding -> {
          throw ErrorCode.SECURITY_NOT_FOUND_IN_SM.toExceptionForHolding(holding, holding.getIdsString());
        });
    sentToSm.stream()
        .filter(holding -> isReturnsEmpty(originalMonthlyReturns.get(holding)))
        .findFirst()
        .ifPresent(holding -> {
          throw ErrorCode.MISSING_MONTHLY_RETURNS.toExceptionForHolding(holding);
        });
  }

  private boolean isReturnsEmpty(HoldingMonthlyReturns holdingReturns) {
    return CollectionUtils.isEmpty(holdingReturns.getReturns());
  }

  public ReturnsAggregate<HoldingMonthlyReturns> getMonthlyReturns(
      Map<PortfolioHolding, HoldingMonthlyReturns> originalMonthlyReturns) {
    return new ReturnsAggregate<>(originalMonthlyReturns);
  }

  public ReturnsAggregate<HoldingMonthlyReturns> getMonthlyReturnsOnlyWithMonthlyReturnsDataValidation(
      List<PortfolioHolding> holdings,
      Currency currency) {
    Map<PortfolioHolding, HoldingMonthlyReturns> originalMonthlyReturns = monthlyReturnsSecurityDataFetcher.fetch(
        holdings, List
            .of());
    return ReturnsAggregate.initOnlyWithReturnsDataValidation(originalMonthlyReturns);
  }

  public ReturnsAggregate<HoldingMonthlyReturns> getPortfolioMonthlyReturns(List<PortfolioHolding> holdings,
      Currency currency,
      ReturnFactorScale returnFactorScale) throws CalculationsFailedException {
    ReturnsAggregate<HoldingMonthlyReturns> portfolioMonthlyReturnsAggregate = getMonthlyReturns(holdings, currency);

    portfolioMonthlyReturnsAggregate
        .setFxRateService(fxRateService)
        .setFxRates(fetchFxRates(portfolioMonthlyReturnsAggregate.holdingCurrencyMap, currency,
            portfolioMonthlyReturnsAggregate.getPerformanceStartDate(), portfolioMonthlyReturnsAggregate
                .getPerformanceEndDate()), currency)
        .setMonthlyReturnsCutComponent(new ReturnsCutComponent())
        .setWeightedAverageComponent(new WeightedAverageComponent(returnFactorScale))
        .setCpsdDataValidation(new PortfolioCpsdDataValidation())
        .setCpedDataValidation(new PortfolioCpedDataValidation());

    return portfolioMonthlyReturnsAggregate;
  }

  public ReturnsAggregate<HoldingMonthlyReturns> getBenchmarkMonthlyReturns(List<PortfolioHolding> holdings,
      Currency currency,
      ReturnFactorScale returnFactorScale) {
    ReturnsAggregate<HoldingMonthlyReturns> benchmarkMonthlyReturnsAggregate = getMonthlyReturns(holdings, currency);

    benchmarkMonthlyReturnsAggregate
        .setFxRateService(fxRateService)
        .setFxRates(fetchFxRates(benchmarkMonthlyReturnsAggregate.holdingCurrencyMap, currency,
            benchmarkMonthlyReturnsAggregate.getPerformanceStartDate(), benchmarkMonthlyReturnsAggregate
                .getPerformanceEndDate()), currency)
        .setMonthlyReturnsCutComponent(new ReturnsCutComponent())
        .setWeightedAverageComponent(new WeightedAverageComponent(returnFactorScale))
        .setCpsdDataValidation(new BenchmarkCpsdDataValidation())
        .setCpedDataValidation(new BenchmarkCpedDataValidation());

    return benchmarkMonthlyReturnsAggregate;
  }

  private Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> fetchFxRates(
      Map<PortfolioHolding, Currency> holdingCurrencies, Currency toCurrency,
      LocalDate from, LocalDate to) {
    log.debug("PortfolioHolding currencies: {}, target: {}", holdingCurrencies.values(), toCurrency);
    return fxRateService.rates(holdingCurrencies, toCurrency, new DateRange(from, to));
  }

}
