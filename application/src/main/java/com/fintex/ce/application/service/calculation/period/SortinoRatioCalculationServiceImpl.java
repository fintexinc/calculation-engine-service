package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.DownsideDeviationCalculation;
import com.fintex.ce.application.calculation.SortinoRatioCalculation;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.application.result.SortinoRatioResult;
import com.fintex.ce.adapter.cache.TBillsCacheStorage;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SortinoRatioCalculationServiceImpl extends PeriodAbstractService<SortinoRatioResult, PeriodCommand> {

  private final TBillsCacheStorage tBillsCacheStorage;

  public SortinoRatioCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      final TBillsCacheStorage tBillsCacheStorage,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsCacheStorage = tBillsCacheStorage;
  }

  @Override
  public SortinoRatioCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = tBillsCacheStorage.loadTBillsFor(reqDTO.getCurrency());
    final DownsideDeviationCalculation<SortinoRatioResult> downsideDeviationCalculation = new DownsideDeviationCalculation<>(
        input, defaultPeriods, tBills);
    return new SortinoRatioCalculation(input, defaultPeriods, tBills, downsideDeviationCalculation);
  }
}
