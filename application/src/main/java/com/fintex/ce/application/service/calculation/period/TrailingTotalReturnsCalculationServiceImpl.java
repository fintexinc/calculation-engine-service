package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.TrailingTotalReturnsCalculation;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.application.result.TrailingTotalReturnsResult;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TrailingTotalReturnsCalculationServiceImpl
    extends
      PeriodAbstractService<TrailingTotalReturnsResult, PeriodCommand> {

  public TrailingTotalReturnsCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.trailing-total-returns}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
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
