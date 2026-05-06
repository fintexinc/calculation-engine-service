package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.DownsideDeviationCalculation;
import com.fintex.ce.application.calculation.metric.SortinoRatioCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.SortinoRatioResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SortinoRatioCalculationServiceImpl extends PeriodAbstractService<SortinoRatioResult, PeriodCommand> {

  private final TreasuryBillsFetcher treasuryBillsFetcher;

  public SortinoRatioCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      final TreasuryBillsFetcher treasuryBillsFetcher,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.treasuryBillsFetcher = treasuryBillsFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.SORTINO_RATIO;
  }

  @Override
  public SortinoRatioCalculation defineCalculationMethod(final PeriodCommand command) {
    final PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = TBillsValidator.requireNonEmpty(

        treasuryBillsFetcher.fetch(command.getCurrency()), command.getCurrency());
    final DownsideDeviationCalculation<SortinoRatioResult> downsideDeviationCalculation = new DownsideDeviationCalculation<>(
        input, defaultPeriods, tBills);
    return new SortinoRatioCalculation(input, defaultPeriods, tBills, downsideDeviationCalculation);
  }
}
