package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.SharpeRatioCalculation;
import com.fintex.ce.application.calculation.StandardDeviationCalculation;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.application.result.SharpeRatioResult;
import com.fintex.ce.adapter.cache.TBillsCacheStorage;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SharpeRatioCalculationServiceImpl extends PeriodAbstractService<SharpeRatioResult, PeriodCommand> {

  private final TBillsCacheStorage tBillsCacheStorage;

  public SharpeRatioCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      final TBillsCacheStorage tBillsCacheStorage,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsCacheStorage = tBillsCacheStorage;
  }

  public SharpeRatioCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = tBillsCacheStorage.loadTBillsFor(reqDTO.getCurrency());
    final var standardDeviationCalculation = new StandardDeviationCalculation<SharpeRatioResult>(input, defaultPeriods);
    return new SharpeRatioCalculation(input, defaultPeriods, tBills, standardDeviationCalculation);
  }

}
