package com.fintex.ce.application.returns;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;

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
