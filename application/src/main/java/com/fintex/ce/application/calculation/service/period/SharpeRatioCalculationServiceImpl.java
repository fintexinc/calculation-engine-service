package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.SharpeRatioCalculation;
import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.result.SharpeRatioResult;
import com.fintex.ce.port.webclient.TBillsFetcher;
import com.fintex.ce.util.ReturnFactorScale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

  public SharpeRatioCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = tBillsProvider.fetch(reqDTO.getCurrency());
    final var standardDeviationCalculation = new StandardDeviationCalculation<SharpeRatioResult>(input, defaultPeriods);
    return new SharpeRatioCalculation(input, defaultPeriods, tBills, standardDeviationCalculation);
  }

}
