package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.SharpeRatioCalculation;
import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.SharpeRatioResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TBillsFetcher;
import com.fintex.ce.util.ReturnFactorScale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SharpeRatioCalculationServiceImpl extends PeriodAbstractService<SharpeRatioResult, PeriodCommand> {

  private final TBillsFetcher tBillsProvider;

  public SharpeRatioCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      final TBillsFetcher tBillsProvider,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsProvider = tBillsProvider;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.SHARPE_RATIO;
  }

  public SharpeRatioCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = tBillsProvider.fetch(reqDTO.getCurrency());
    final var standardDeviationCalculation = new StandardDeviationCalculation<SharpeRatioResult>(input, defaultPeriods);
    return new SharpeRatioCalculation(input, defaultPeriods, tBills, standardDeviationCalculation);
  }

}
