package com.fintex.ce.application.service.calculation;

import com.fintex.smclient.dto.FxRatesDTO;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.application.validation.BenchmarkCpedDataValidation;
import com.fintex.ce.application.validation.BenchmarkCpsdDataValidation;
import com.fintex.ce.monthlyreturns.FxRatesConversionComponent;
import com.fintex.ce.monthlyreturns.MonthlyReturnsGenerator;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.monthlyreturns.ReturnsCutComponent;
import com.fintex.ce.monthlyreturns.WeightedAverageComponent;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.exception.FdsDataValidationException;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.port.output.cache.FxRatesProvider;
import com.fintex.ce.port.output.cache.HoldingDataLoader;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

@Service
public class MonthlyReturnsService {

  private final HoldingDataLoader<Map<Holding, MonthlyReturns>> monthlyReturnsCachePort;
  private final FxRatesProvider fxRatesProvider;
  private final MonthlyReturnsGenerator monthlyReturnsGenerator;

  public MonthlyReturnsService(HoldingDataLoader<Map<Holding, MonthlyReturns>> monthlyReturnsCachePort,
      FxRatesProvider fxRatesProvider,
      MonthlyReturnsGenerator monthlyReturnsGenerator) {
    this.monthlyReturnsCachePort = monthlyReturnsCachePort;
    this.fxRatesProvider = fxRatesProvider;
    this.monthlyReturnsGenerator = monthlyReturnsGenerator;
  }

  public NavigableMap<LocalDate, BigDecimal> getWeightedAverageWithCpsdAndCpedValidation(
      Returns<MonthlyReturns> monthlyReturns,
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
      Returns<MonthlyReturns> monthlyReturns,
      LocalDate cped) {
    return monthlyReturns
        .validateCped(cped)
        .cutByCpedIfCpedEmptyCutByPed(cped)
        .cutByPsd()
        .fxRatesApplied()
        .getWeightedAverage();
  }

  public Returns<MonthlyReturns> getMonthlyReturns(List<Holding> holdings, Currency currency) {
    Map<Holding, MonthlyReturns> originalMonthlyReturns = monthlyReturnsCachePort.load(holdings, List.of(), List
        .of(), new ParamHolderDTO(currency));
    originalMonthlyReturns.putAll(monthlyReturnsGenerator.generateGicMonthlyReturns(holdings));
    return getMonthlyReturns(originalMonthlyReturns);
  }

  public Returns<MonthlyReturns> getMonthlyReturns(Map<Holding, MonthlyReturns> originalMonthlyReturns) {
    return new Returns<>(originalMonthlyReturns);
  }

  public Returns<MonthlyReturns> getMonthlyReturnsOnlyWithMonthlyReturnsDataValidation(List<Holding> holdings,
      Currency currency) {
    Map<Holding, MonthlyReturns> originalMonthlyReturns = monthlyReturnsCachePort.load(holdings, List.of(), List
        .of(), new ParamHolderDTO(currency));
    return Returns.initOnlyWithReturnsDataValidation(originalMonthlyReturns);
  }

  public Returns<MonthlyReturns> getPortfolioMonthlyReturns(final List<Holding> holdings,
      final Currency currency,
      final ReturnFactorScale returnFactorScale) throws FdsDataValidationException {
    Returns<MonthlyReturns> portfolioMonthlyReturns = getMonthlyReturns(holdings, currency);

    portfolioMonthlyReturns
        .setFxRatesConversionComponent(new FxRatesConversionComponent(getFxRates(), currency))
        .setMonthlyReturnsCutComponent(new ReturnsCutComponent())
        .setWeightedAverageComponent(new WeightedAverageComponent(returnFactorScale))
        .setCpsdDataValidation(new PortfolioCpsdDataValidation())
        .setCpedDataValidation(new PortfolioCpedDataValidation());

    return portfolioMonthlyReturns;
  }

  public Returns<MonthlyReturns> getBenchmarkMonthlyReturns(final List<Holding> holdings,
      final Currency currency,
      final ReturnFactorScale returnFactorScale) {
    Returns<MonthlyReturns> benchmarkMonthlyReturns = getMonthlyReturns(holdings, currency);

    benchmarkMonthlyReturns
        .setFxRatesConversionComponent(new FxRatesConversionComponent(getFxRates(), currency))
        .setMonthlyReturnsCutComponent(new ReturnsCutComponent())
        .setWeightedAverageComponent(new WeightedAverageComponent(returnFactorScale))
        .setCpsdDataValidation(new BenchmarkCpsdDataValidation())
        .setCpedDataValidation(new BenchmarkCpedDataValidation());

    return benchmarkMonthlyReturns;
  }

  public Map<LocalDate, FxRatesDTO> getFxRates() {
    return fxRatesProvider.loadFxRates();
  }

}
