package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.DownsideDeviationCalculation;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.port.input.result.DownsideDeviationResult;
import com.fintex.ce.port.output.TBillsPort;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DownsideDeviationCalculationServiceImpl
    extends
      PeriodAbstractService<DownsideDeviationResult, PeriodCommand> {

  private final TBillsPort tBillsProvider;

  public DownsideDeviationCalculationServiceImpl(
      @Autowired final MonthlyReturnsService monthlyReturnsService,
      @Autowired  final TBillsPort tBillsProvider,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsProvider = tBillsProvider;
  }

  public DownsideDeviationCalculation<DownsideDeviationResult> defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = tBillsProvider.loadTBillsFor(reqDTO.getCurrency());
    return new DownsideDeviationCalculation<>(inputDTO, defaultPeriods, tBills);
  }
}
