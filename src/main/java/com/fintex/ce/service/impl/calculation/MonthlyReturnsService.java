package com.fintex.ce.service.impl.calculation;

import com.fintex.smclient.dto.FxRatesDTO;
import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.domain.monthlyreturns.BenchmarkCpedDataValidation;
import com.fintex.ce.domain.monthlyreturns.BenchmarkCpsdDataValidation;
import com.fintex.ce.domain.monthlyreturns.FxRatesConversionComponent;
import com.fintex.ce.domain.monthlyreturns.MonthlyReturnsGenerator;
import com.fintex.ce.domain.monthlyreturns.PortfolioCpedDataValidation;
import com.fintex.ce.domain.monthlyreturns.PortfolioCpsdDataValidation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.domain.monthlyreturns.ReturnsCutComponent;
import com.fintex.ce.domain.monthlyreturns.WeightedAverageComponent;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.exception.FdsDataValidationException;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.service.impl.cache.FxRatesCacheStorage;
import com.fintex.ce.service.impl.cache.MonthlyReturnsCacheStorage;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

@Service
public class MonthlyReturnsService {

    private final MonthlyReturnsCacheStorage monthlyReturnsCacheStorage;
    private final FxRatesCacheStorage fxRatesCacheStorage;
    private final MonthlyReturnsGenerator monthlyReturnsGenerator;


    public MonthlyReturnsService(MonthlyReturnsCacheStorage monthlyReturnsCacheStorage,
                                 FxRatesCacheStorage fxRatesCacheStorage,
                                 MonthlyReturnsGenerator monthlyReturnsGenerator) {
        this.monthlyReturnsCacheStorage = monthlyReturnsCacheStorage;
        this.fxRatesCacheStorage = fxRatesCacheStorage;
        this.monthlyReturnsGenerator = monthlyReturnsGenerator;
    }

    public NavigableMap<LocalDate, BigDecimal> getWeightedAverageWithCpsdAndCpedValidation(Returns<RMonthlyReturns> monthlyReturns,
                                                                                           LocalDate cpsd, LocalDate cped) {
        return monthlyReturns
                .validateCped(cped)
                .validateCpsd(cpsd)
                .cutByCpedIfCpedEmptyCutByPed(cped)
                .cutByCpsdIfCpsdEmptyCutByPsd(cpsd)
                .fxRatesApplied()
                .getWeightedAverage();
    }

    public NavigableMap<LocalDate, BigDecimal> getWeightedAverageWithCpedValidation(Returns<RMonthlyReturns> monthlyReturns,
                                                                                    LocalDate cped) {
        return monthlyReturns
                .validateCped(cped)
                .cutByCpedIfCpedEmptyCutByPed(cped)
                .cutByPsd()
                .fxRatesApplied()
                .getWeightedAverage();
    }

    public Returns<RMonthlyReturns> getMonthlyReturns(List<Holding> holdings, Currency currency) {
        Map<Holding, RMonthlyReturns> originalMonthlyReturns = monthlyReturnsCacheStorage.load(holdings, List.of(), List.of(), new ParamHolderDTO(currency));
        originalMonthlyReturns.putAll(monthlyReturnsGenerator.generateGicMonthlyReturns(holdings));
        return getMonthlyReturns(originalMonthlyReturns);
    }

    Returns<RMonthlyReturns> getMonthlyReturns(Map<Holding, RMonthlyReturns> originalMonthlyReturns) {
        return new Returns<>(originalMonthlyReturns);
    }

    public Returns<RMonthlyReturns> getMonthlyReturnsOnlyWithMonthlyReturnsDataValidation(List<Holding> holdings, Currency currency) {
        Map<Holding, RMonthlyReturns> originalMonthlyReturns = monthlyReturnsCacheStorage.load(holdings, List.of(), List.of(), new ParamHolderDTO(currency));
        return Returns.initOnlyWithReturnsDataValidation(originalMonthlyReturns);
    }

    public Returns<RMonthlyReturns> getPortfolioMonthlyReturns(final List<Holding> holdings,
                                              final Currency currency,
                                              final ReturnFactorScale returnFactorScale) throws FdsDataValidationException {
        Returns<RMonthlyReturns> portfolioMonthlyReturns = getMonthlyReturns(holdings, currency);

        portfolioMonthlyReturns
                .setFxRatesConversionComponent(new FxRatesConversionComponent(getFxRates(), currency))
                .setMonthlyReturnsCutComponent(new ReturnsCutComponent())
                .setWeightedAverageComponent(new WeightedAverageComponent(returnFactorScale))
                .setCpsdDataValidation(new PortfolioCpsdDataValidation())
                .setCpedDataValidation(new PortfolioCpedDataValidation());

        return portfolioMonthlyReturns;
    }

    public Returns<RMonthlyReturns> getBenchmarkMonthlyReturns(final List<Holding> holdings,
                                              final Currency currency,
                                              final ReturnFactorScale returnFactorScale) {
        Returns<RMonthlyReturns> benchmarkMonthlyReturns = getMonthlyReturns(holdings, currency);

        benchmarkMonthlyReturns
                .setFxRatesConversionComponent(new FxRatesConversionComponent(getFxRates(), currency))
                .setMonthlyReturnsCutComponent(new ReturnsCutComponent())
                .setWeightedAverageComponent(new WeightedAverageComponent(returnFactorScale))
                .setCpsdDataValidation(new BenchmarkCpsdDataValidation())
                .setCpedDataValidation(new BenchmarkCpedDataValidation());

        return benchmarkMonthlyReturns;
    }

    public Map<LocalDate, FxRatesDTO> getFxRates() {
        return fxRatesCacheStorage.loadFxRates();
    }

}
