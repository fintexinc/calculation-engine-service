package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.result.TrailingTotalReturnsResult;
import com.fintex.ce.util.ReturnFactorScale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TrailingTotalReturnsCalculationServiceImpl
    extends
      PeriodAbstractService<TrailingTotalReturnsResult, PeriodCommand> {

  public TrailingTotalReturnsCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.trailing-total-returns}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.TRAILING_TOTAL_RETURNS;
  }

  public TrailingTotalReturnsCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    return new TrailingTotalReturnsCalculation(inputDTO, defaultPeriods);
  }

  @Override
  public void addSpecificChecks(final PeriodCommand reqDTO) {
    // Empty as there are no specific checks for the current calculation
  }

}
