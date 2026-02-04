package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.BetaCalculation;
import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.TreynorRatioCalculation;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.application.result.TreynorRatioResult;
import com.fintex.ce.adapter.cache.TBillsCacheStorage;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.application.calculation.core.PeriodCalculationAbstract.calculateExcessReturn;

@Service
public class TreynorRatioServiceImpl extends PeriodBenchmarkAbstractService<TreynorRatioResult, PeriodCommand> {

  private final TBillsCacheStorage tBillsCacheStorage;

  public TreynorRatioServiceImpl(
      MonthlyReturnsService monthlyReturnsService,
      TBillsCacheStorage tBillsCacheStorage,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsCacheStorage = tBillsCacheStorage;
  }

  @Override
  public PeriodCalculationAbstract<TreynorRatioResult, ?> defineCalculationMethod(PeriodCommand reqDTO) {
    BenchmarkCalculationDTO betaInput = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    BenchmarkCalculationDTO treynorRationInput = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    var tBills = tBillsCacheStorage.loadTBillsFor(reqDTO.getCurrency());
    NavigableMap<LocalDate, BigDecimal> portfolioExccessReturn = calculateExcessReturn(betaInput
        .getWeightedAveragePortfolioReturns(), tBills);
    NavigableMap<LocalDate, BigDecimal> benchmarkExccessReturn = calculateExcessReturn(betaInput
        .getWeightedAverageBenchmarkReturns(), tBills);
    var betaCalculation = new BetaCalculation(betaInput, defaultPeriods, portfolioExccessReturn,
        benchmarkExccessReturn);
    return new TreynorRatioCalculation(treynorRationInput, defaultPeriods, tBills, betaCalculation);
  }

}
