package ca.tangerine.pce.application.returns;

import org.springframework.stereotype.Component;

import ca.tangerine.pce.application.calculation.service.FxRateService;
import ca.tangerine.pce.application.calculation.service.MonthlyReturnsService;

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
