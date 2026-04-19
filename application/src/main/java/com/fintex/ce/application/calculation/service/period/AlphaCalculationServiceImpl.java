package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.AlphaCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.AlphaResult;
import com.fintex.ce.model.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TBillsFetcher;
import com.fintex.ce.util.ReturnFactorScale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract.calculateExcessReturn;

@Service
public class AlphaCalculationServiceImpl extends PeriodBenchmarkAbstractService<AlphaResult, PeriodCommand> {

  private final TBillsFetcher tBillsProvider;

  public AlphaCalculationServiceImpl(
      @Autowired final MonthlyReturnsService monthlyReturnsService,
      @Autowired final TBillsFetcher tBillsProvider,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsProvider = tBillsProvider;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ALPHA;
  }

  @Override
  public PeriodCalculationAbstract<AlphaResult, ?> defineCalculationMethod(final PeriodCommand reqDTO) {
    final BenchmarkCalculationDTO inDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = tBillsProvider.fetch(reqDTO.getCurrency());
    final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn = calculateExcessReturn(inDTO
        .getWeightedAveragePortfolioReturns(), tBills);
    final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn = calculateExcessReturn(inDTO
        .getWeightedAverageBenchmarkReturns(), tBills);
    return new AlphaCalculation(inDTO, defaultPeriods, portfolioExcessReturn, benchmarkExcessReturn);
  }

}
