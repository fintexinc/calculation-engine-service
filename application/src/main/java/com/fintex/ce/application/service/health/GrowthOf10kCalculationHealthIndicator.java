package com.fintex.ce.application.service.health;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.application.command.ReturnCommand;
import com.fintex.ce.application.result.Growth10KResult;
import com.fintex.ce.service.calculation.CalculationService;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class GrowthOf10kCalculationHealthIndicator extends CalculationHeathIndicator<ReturnCommand> {

  private final CalculationService<Growth10KResult, ReturnCommand> growthOf10KCalculationService;

  public GrowthOf10kCalculationHealthIndicator(
      final CalculationService<Growth10KResult, ReturnCommand> growthOf10KCalculationService) {
    this.growthOf10KCalculationService = growthOf10KCalculationService;
  }

  @Override
  public Growth10KResult calculateResponse(final ReturnCommand request) {
    return growthOf10KCalculationService.perform(request);
  }

  @Override
  public ReturnCommand buildInput() {
    final ReturnCommand request = new ReturnCommand();
    request.setHoldings(getHoldings());
    request.setCurrency(Currency.CAD);
    request.setCustomPerformanceStartDate(LocalDate.of(2015, 6, 30));
    request.setCustomPerformanceEndDate(LocalDate.of(2016, 6, 30));
    return request;
  }

}
