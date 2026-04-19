package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.FxRatesConversionComponent;
import com.fintex.ce.application.returns.MonthlyReturnsGenerator;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.returns.ReturnsCutComponent;
import com.fintex.ce.application.returns.WeightedAverageComponent;
import com.fintex.ce.application.validation.BenchmarkCpedDataValidation;
import com.fintex.ce.application.validation.BenchmarkCpsdDataValidation;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;
import com.fintex.ce.port.webclient.boc.FxRatesFetcher;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.wm.commons.domain.currency.Currency;

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
    return getMonthlyReturns(originalMonthlyReturns);
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

  public ReturnsAggregate<HoldingMonthlyReturns> getPortfolioMonthlyReturns(final List<PortfolioHolding> holdings,
      final Currency currency,
      final ReturnFactorScale returnFactorScale) throws CalculationsFailedException {
    ReturnsAggregate<HoldingMonthlyReturns> portfolioMonthlyReturnsAggregate = getMonthlyReturns(holdings, currency);

    portfolioMonthlyReturnsAggregate
        .setFxRatesConversionComponent(new FxRatesConversionComponent())
        .setFxRates(fetchFxRates(portfolioMonthlyReturnsAggregate.holdingCurrencyMap, currency,
            portfolioMonthlyReturnsAggregate.getPsd(), portfolioMonthlyReturnsAggregate.getPed()), currency)
        .setMonthlyReturnsCutComponent(new ReturnsCutComponent())
        .setWeightedAverageComponent(new WeightedAverageComponent(returnFactorScale))
        .setCpsdDataValidation(new PortfolioCpsdDataValidation())
        .setCpedDataValidation(new PortfolioCpedDataValidation());

    return portfolioMonthlyReturnsAggregate;
  }

  public ReturnsAggregate<HoldingMonthlyReturns> getBenchmarkMonthlyReturns(final List<PortfolioHolding> holdings,
      final Currency currency,
      final ReturnFactorScale returnFactorScale) {
    ReturnsAggregate<HoldingMonthlyReturns> benchmarkMonthlyReturnsAggregate = getMonthlyReturns(holdings, currency);

    benchmarkMonthlyReturnsAggregate
        .setFxRatesConversionComponent(new FxRatesConversionComponent())
        .setFxRates(fetchFxRates(benchmarkMonthlyReturnsAggregate.holdingCurrencyMap, currency,
            benchmarkMonthlyReturnsAggregate.getPsd(), benchmarkMonthlyReturnsAggregate.getPed()), currency)
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
    DateRange dateRange = new DateRange(from, to);
    return holdingCurrencies.values().stream()
        .distinct()
        .filter(fromCurrency -> !fromCurrency.equals(toCurrency))
        .collect(Collectors.toMap(
            fromCurrency -> new CurrencyExchangePair(fromCurrency, toCurrency),
            fromCurrency -> fxRatesFetcher.fetch(new CurrencyExchangePair(fromCurrency, toCurrency), dateRange)));
  }

}
