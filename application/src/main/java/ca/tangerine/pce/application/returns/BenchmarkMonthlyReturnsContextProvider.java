package ca.tangerine.pce.application.returns;

import ca.tangerine.pce.application.calculation.service.FxRateService;
import ca.tangerine.pce.application.calculation.service.MonthlyReturnsService;

import org.springframework.stereotype.Component;

@Component
public class BenchmarkMonthlyReturnsContextProvider extends MonthlyReturnsContextProvider {

  public BenchmarkMonthlyReturnsContextProvider(MonthlyReturnsService monthlyReturnsService,
      FxRateService fxRateService) {
    super(monthlyReturnsService, fxRateService);
  }

  @Override
  protected ReturnsRole role() {
    return ReturnsRole.BENCHMARK;
  }
}
