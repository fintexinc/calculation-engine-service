package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.BetaCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.domain.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.result.BetaResult;
import com.fintex.ce.port.webclient.TBillsFetcher;
import com.fintex.ce.util.ReturnFactorScale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import static com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract.calculateExcessReturn;

@Service
public class BetaCalculationServiceImpl extends PeriodBenchmarkAbstractService<BetaResult, PeriodCommand> {

  private final TBillsFetcher tBillsProvider;

  public BetaCalculationServiceImpl(
      @Autowired final MonthlyReturnsService monthlyReturnsService,
      @Autowired  final TBillsFetcher tBillsProvider,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsProvider = tBillsProvider;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.BETA;
  }

  @Override
  public PeriodCalculationAbstract<BetaResult, ?> defineCalculationMethod(final PeriodCommand reqDTO) {
    final BenchmarkCalculationDTO inDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    final var tBills = tBillsProvider.fetch(reqDTO.getCurrency());
    final NavigableMap<LocalDate, BigDecimal> portfolioExccessReturn = calculateExcessReturn(inDTO
        .getWeightedAveragePortfolioReturns(), tBills);
    final NavigableMap<LocalDate, BigDecimal> benchmarkExccessReturn = calculateExcessReturn(inDTO
        .getWeightedAverageBenchmarkReturns(), tBills);
    return new BetaCalculation(inDTO, defaultPeriods, portfolioExccessReturn, benchmarkExccessReturn);
  }

}
