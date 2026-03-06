package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.AlphaCalculation;
import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.port.input.result.AlphaResult;
import com.fintex.ce.port.output.cache.TBillsProvider;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.application.calculation.core.PeriodCalculationAbstract.calculateExcessReturn;

@Service
public class AlphaCalculationServiceImpl extends PeriodBenchmarkAbstractService<AlphaResult, PeriodCommand> {

  private final TBillsProvider tBillsProvider;

  public AlphaCalculationServiceImpl(
      @Autowired final MonthlyReturnsService monthlyReturnsService,
      @Autowired final TBillsProvider tBillsProvider,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsProvider = tBillsProvider;
  }

  @Override
  public PeriodCalculationAbstract<AlphaResult, ?> defineCalculationMethod(final PeriodCommand reqDTO) {
    final BenchmarkCalculationDTO inDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = tBillsProvider.loadTBillsFor(reqDTO.getCurrency());
    final NavigableMap<LocalDate, BigDecimal> portfolioExccessReturn = calculateExcessReturn(inDTO
        .getWeightedAveragePortfolioReturns(), tBills);
    final NavigableMap<LocalDate, BigDecimal> benchmarkExccessReturn = calculateExcessReturn(inDTO
        .getWeightedAverageBenchmarkReturns(), tBills);
    return new AlphaCalculation(inDTO, defaultPeriods, portfolioExccessReturn, benchmarkExccessReturn);
  }

}
