package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TrailingTotalReturnsCalculationServiceImpl
    extends
      PeriodAbstractService<TrailingTotalReturnsResult, PeriodCommand> {

  private final TreasuryBillsFetcher treasuryBillsFetcher;

  public TrailingTotalReturnsCalculationServiceImpl(
      MonthlyReturnsService monthlyReturnsService,
      TreasuryBillsFetcher treasuryBillsFetcher,
      @Value("#{'${default.periods.trailing-total-returns}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.treasuryBillsFetcher = treasuryBillsFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.TRAILING_TOTAL_RETURNS;
  }

  public TrailingTotalReturnsCalculation defineCalculationMethod(PeriodCommand command) {
    PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    var tBills = TBillsValidator.requireNonEmpty(
        treasuryBillsFetcher.fetch(command.getCurrency()), command.getCurrency());
    return new TrailingTotalReturnsCalculation(input, defaultPeriods, tBills);
  }

}