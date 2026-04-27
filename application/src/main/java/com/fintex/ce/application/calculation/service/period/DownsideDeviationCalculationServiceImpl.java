package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.DownsideDeviationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.DownsideDeviationResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TBillsFetcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DownsideDeviationCalculationServiceImpl
    extends
      PeriodAbstractService<DownsideDeviationResult, PeriodCommand> {

  private final TBillsFetcher tBillsProvider;

  public DownsideDeviationCalculationServiceImpl(
      @Autowired final MonthlyReturnsService monthlyReturnsService,
      @Autowired final TBillsFetcher tBillsProvider,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsProvider = tBillsProvider;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.DOWNSIDE_DEVIATION;
  }

  public DownsideDeviationCalculation<DownsideDeviationResult> defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = tBillsProvider.fetch(reqDTO.getCurrency());
    return new DownsideDeviationCalculation<>(inputDTO, defaultPeriods, tBills);
  }
}
