package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.RSquaredCalculation;
import com.fintex.ce.application.result.RSquaredResult;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.adapter.cache.TBillsCacheStorage;
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
public class RSquaredCalculationServiceImpl extends PeriodBenchmarkAbstractService<RSquaredResult, PeriodCommand> {

  private final TBillsCacheStorage tBillsCacheStorage;

  public RSquaredCalculationServiceImpl(
      @Autowired final MonthlyReturnsService monthlyReturnsService,
      @Autowired final TBillsCacheStorage tBillsCacheStorage,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsCacheStorage = tBillsCacheStorage;
  }

  @Override
  public PeriodCalculationAbstract<RSquaredResult, ?> defineCalculationMethod(final PeriodCommand reqDTO) {
    final BenchmarkCalculationDTO inDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    final var tBills = tBillsCacheStorage.loadTBillsFor(reqDTO.getCurrency());
    final NavigableMap<LocalDate, BigDecimal> portfolioExccessReturn = calculateExcessReturn(inDTO
        .getWeightedAveragePortfolioReturns(), tBills);
    final NavigableMap<LocalDate, BigDecimal> benchmarkExccessReturn = calculateExcessReturn(inDTO
        .getWeightedAverageBenchmarkReturns(), tBills);
    return new RSquaredCalculation(inDTO, defaultPeriods, portfolioExccessReturn, benchmarkExccessReturn);
  }

}
