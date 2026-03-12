package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.BetaCalculation;
import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.TreynorRatioCalculation;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.port.input.result.TreynorRatioResult;
import com.fintex.ce.port.output.TBillsPort;
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

  private final TBillsPort tBillsProvider;

  public TreynorRatioServiceImpl(
      MonthlyReturnsService monthlyReturnsService,
      TBillsPort tBillsProvider,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsProvider = tBillsProvider;
  }

  @Override
  public PeriodCalculationAbstract<TreynorRatioResult, ?> defineCalculationMethod(PeriodCommand reqDTO) {
    BenchmarkCalculationDTO betaInput = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    BenchmarkCalculationDTO treynorRatioInput = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    var tBills = tBillsProvider.loadTBillsFor(reqDTO.getCurrency());
    NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn = calculateExcessReturn(betaInput
        .getWeightedAveragePortfolioReturns(), tBills);
    NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn = calculateExcessReturn(betaInput
        .getWeightedAverageBenchmarkReturns(), tBills);
    var betaCalculation = new BetaCalculation(betaInput, defaultPeriods, portfolioExcessReturn,
        benchmarkExcessReturn);
    return new TreynorRatioCalculation(treynorRatioInput, defaultPeriods, tBills, betaCalculation);
  }

}
