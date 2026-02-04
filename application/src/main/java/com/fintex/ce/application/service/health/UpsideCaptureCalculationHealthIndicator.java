package com.fintex.ce.application.service.health;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.application.result.UpsideCaptureResult;
import com.fintex.ce.application.service.calculation.period.UpsideCaptureCalculationServiceImpl;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
public class UpsideCaptureCalculationHealthIndicator extends CalculationHeathIndicator<PeriodCommand> {

  private final UpsideCaptureCalculationServiceImpl upsideCaptureCalculationService;

  public UpsideCaptureCalculationHealthIndicator(
      final UpsideCaptureCalculationServiceImpl upsideCaptureCalculationService) {
    this.upsideCaptureCalculationService = upsideCaptureCalculationService;
  }

  @Override
  public UpsideCaptureResult calculateResponse(final PeriodCommand periodsReqDTO) {
    return upsideCaptureCalculationService.perform(periodsReqDTO);
  }

  @Override
  public PeriodCommand buildInput() {
    return (PeriodCommand) new PeriodCommand()
        .setCustomIntervalPsd(LocalDate.of(2019, 1, 31))
        .setCustomPed(LocalDate.of(2019, 6, 30))
        .setPeriods(Set.of("12", "36", "60", "120"))
        .setCurrency(Currency.CAD)
        .setBenchmarkHoldings(getHoldings())
        .setHoldings(getHoldings());
  }

}
