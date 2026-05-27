package com.fintex.ce.application.returns;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;

import org.springframework.stereotype.Component;

@Component
public class PortfolioMonthlyReturnsContextProvider extends MonthlyReturnsContextProvider {

  public PortfolioMonthlyReturnsContextProvider(MonthlyReturnsService monthlyReturnsService,
      FxRateService fxRateService) {
    super(monthlyReturnsService, fxRateService);
  }

  @Override
  protected ReturnsRole role() {
    return ReturnsRole.PORTFOLIO;
  }
}
