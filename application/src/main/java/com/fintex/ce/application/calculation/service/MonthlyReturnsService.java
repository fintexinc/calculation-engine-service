package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.FxRatesConversionComponent;
import com.fintex.ce.application.returns.MonthlyReturnsGenerator;
import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.application.returns.ReturnsCutComponent;
import com.fintex.ce.application.returns.WeightedAverageComponent;
import com.fintex.ce.application.validation.BenchmarkCpedDataValidation;
import com.fintex.ce.application.validation.BenchmarkCpsdDataValidation;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.domain.exception.FdsDataValidationException;
import com.fintex.ce.domain.model.CurrencyExchangePair;
import com.fintex.ce.domain.model.DateRange;
import com.fintex.ce.domain.model.HoldingMonthlyReturns;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.webclient.FxRatesFetcher;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MonthlyReturnsService {

  private final SecurityDataFetcher<HoldingMonthlyReturns> monthlyReturnsSecurityDataFetcher;
  private final FxRatesFetcher fxRatesFetcher;
  private final MonthlyReturnsGenerator monthlyReturnsGenerator;

  public MonthlyReturnsService(SecurityDataFetcher<HoldingMonthlyReturns> monthlyReturnsSecurityDataFetcher,
      FxRatesFetcher fxRatesFetcher,
      MonthlyReturnsGenerator monthlyReturnsGenerator) {
    this.monthlyReturnsSecurityDataFetcher = monthlyReturnsSecurityDataFetcher;
    this.fxRatesFetcher = fxRatesFetcher;
    this.monthlyReturnsGenerator = monthlyReturnsGenerator;
  }

  public NavigableMap<LocalDate, BigDecimal> getWeightedAverageWithCpsdAndCpedValidation(
      Returns<HoldingMonthlyReturns> monthlyReturns,
      LocalDate cpsd, LocalDate cped) {
    return monthlyReturns
        .validateCped(cped)
        .validateCpsd(cpsd)
        .cutByCpedIfCpedEmptyCutByPed(cped)
        .cutByCpsdIfCpsdEmptyCutByPsd(cpsd)
        .fxRatesApplied()
        .getWeightedAverage();
  }

  public NavigableMap<LocalDate, BigDecimal> getWeightedAverageWithCpedValidation(
      Returns<HoldingMonthlyReturns> monthlyReturns,
      LocalDate cped) {
    return monthlyReturns
        .validateCped(cped)
        .cutByCpedIfCpedEmptyCutByPed(cped)
        .cutByPsd()
        .fxRatesApplied()
        .getWeightedAverage();
  }

  public Returns<HoldingMonthlyReturns> getMonthlyReturns(List<Holding> holdings, CurrencyType currency) {
    Map<Holding, HoldingMonthlyReturns> originalMonthlyReturns = monthlyReturnsSecurityDataFetcher.fetch(holdings, List
        .of());
    originalMonthlyReturns.putAll(monthlyReturnsGenerator.generateGicMonthlyReturns(holdings));
    return getMonthlyReturns(originalMonthlyReturns);
  }

  public Returns<HoldingMonthlyReturns> getMonthlyReturns(Map<Holding, HoldingMonthlyReturns> originalMonthlyReturns) {
    return new Returns<>(originalMonthlyReturns);
  }

  public Returns<HoldingMonthlyReturns> getMonthlyReturnsOnlyWithMonthlyReturnsDataValidation(List<Holding> holdings,
      CurrencyType currency) {
    Map<Holding, HoldingMonthlyReturns> originalMonthlyReturns = monthlyReturnsSecurityDataFetcher.fetch(holdings, List
        .of());
    return Returns.initOnlyWithReturnsDataValidation(originalMonthlyReturns);
  }

  public Returns<HoldingMonthlyReturns> getPortfolioMonthlyReturns(final List<Holding> holdings,
      final CurrencyType currency,
      final ReturnFactorScale returnFactorScale) throws FdsDataValidationException {
    Returns<HoldingMonthlyReturns> portfolioMonthlyReturns = getMonthlyReturns(holdings, currency);

    portfolioMonthlyReturns
        .setFxRatesConversionComponent(new FxRatesConversionComponent())
        .setFxRates(fetchFxRates(portfolioMonthlyReturns.holdingCurrencyMap, currency,
            portfolioMonthlyReturns.getPsd(), portfolioMonthlyReturns.getPed()), currency)
        .setMonthlyReturnsCutComponent(new ReturnsCutComponent())
        .setWeightedAverageComponent(new WeightedAverageComponent(returnFactorScale))
        .setCpsdDataValidation(new PortfolioCpsdDataValidation())
        .setCpedDataValidation(new PortfolioCpedDataValidation());

    return portfolioMonthlyReturns;
  }

  public Returns<HoldingMonthlyReturns> getBenchmarkMonthlyReturns(final List<Holding> holdings,
      final CurrencyType currency,
      final ReturnFactorScale returnFactorScale) {
    Returns<HoldingMonthlyReturns> benchmarkMonthlyReturns = getMonthlyReturns(holdings, currency);

    benchmarkMonthlyReturns
        .setFxRatesConversionComponent(new FxRatesConversionComponent())
        .setFxRates(fetchFxRates(benchmarkMonthlyReturns.holdingCurrencyMap, currency,
            benchmarkMonthlyReturns.getPsd(), benchmarkMonthlyReturns.getPed()), currency)
        .setMonthlyReturnsCutComponent(new ReturnsCutComponent())
        .setWeightedAverageComponent(new WeightedAverageComponent(returnFactorScale))
        .setCpsdDataValidation(new BenchmarkCpsdDataValidation())
        .setCpedDataValidation(new BenchmarkCpedDataValidation());

    return benchmarkMonthlyReturns;
  }

  private Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> fetchFxRates(
      Map<Holding, CurrencyType> holdingCurrencies, CurrencyType toCurrency,
      LocalDate from, LocalDate to) {
    log.debug("Holding currencies: {}, target: {}", holdingCurrencies.values(), toCurrency);
    DateRange dateRange = from != null && to != null ? new DateRange(from, to) : null;
    return holdingCurrencies.values().stream()
        .distinct()
        .filter(fromCurrency -> !fromCurrency.equals(toCurrency))
        .collect(Collectors.toMap(
            fromCurrency -> new CurrencyExchangePair(fromCurrency, toCurrency),
            fromCurrency -> fxRatesFetcher.fetch(new CurrencyExchangePair(fromCurrency, toCurrency), dateRange)));
  }

}
