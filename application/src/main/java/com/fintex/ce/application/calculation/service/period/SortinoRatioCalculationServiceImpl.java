package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.DownsideDeviationCalculation;
import com.fintex.ce.application.calculation.metric.SortinoRatioCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.result.SortinoRatioResult;
import com.fintex.ce.port.webclient.TBillsFetcher;
import com.fintex.ce.util.ReturnFactorScale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SortinoRatioCalculationServiceImpl extends PeriodAbstractService<SortinoRatioResult, PeriodCommand> {

  private final TBillsFetcher tBillsProvider;

  public SortinoRatioCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      final TBillsFetcher tBillsProvider,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsProvider = tBillsProvider;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.SORTINO_RATIO;
  }

  @Override
  public SortinoRatioCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = tBillsProvider.fetch(reqDTO.getCurrency());
    final DownsideDeviationCalculation<SortinoRatioResult> downsideDeviationCalculation = new DownsideDeviationCalculation<>(
        input, defaultPeriods, tBills);
    return new SortinoRatioCalculation(input, defaultPeriods, tBills, downsideDeviationCalculation);
  }
}
