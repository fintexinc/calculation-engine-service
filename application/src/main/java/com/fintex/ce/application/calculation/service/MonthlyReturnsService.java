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
import com.fintex.ce.domain.model.FxRates;
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

@Service
public class MonthlyReturnsService {

  private final SecurityDataFetcher<HoldingMonthlyReturns> monthlyReturnsSecurityDataFetcher;
  private final FxRatesFetcher fxRatesProvider;
  private final MonthlyReturnsGenerator monthlyReturnsGenerator;

  public MonthlyReturnsService(SecurityDataFetcher<HoldingMonthlyReturns> monthlyReturnsSecurityDataFetcher,
      FxRatesFetcher fxRatesProvider,
      MonthlyReturnsGenerator monthlyReturnsGenerator) {
    this.monthlyReturnsSecurityDataFetcher = monthlyReturnsSecurityDataFetcher;
    this.fxRatesProvider = fxRatesProvider;
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
        .setFxRatesConversionComponent(new FxRatesConversionComponent(getFxRates(), currency))
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
        .setFxRatesConversionComponent(new FxRatesConversionComponent(getFxRates(), currency))
        .setMonthlyReturnsCutComponent(new ReturnsCutComponent())
        .setWeightedAverageComponent(new WeightedAverageComponent(returnFactorScale))
        .setCpsdDataValidation(new BenchmarkCpsdDataValidation())
        .setCpedDataValidation(new BenchmarkCpedDataValidation());

    return benchmarkMonthlyReturns;
  }

  public Map<LocalDate, FxRates.FxRate> getFxRates() {
    return fxRatesProvider.fetch();
  }

}
