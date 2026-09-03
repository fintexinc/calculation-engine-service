package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.value.DateBigDecimalValue;

import java.util.List;

/**
 * Shared e2e infrastructure for {@link ReturnCommand}-based metrics that use the generic portfolio calculation
 * scenarios from {@link AbstractPortfolioCalculationE2ETest}. Common ReturnCommand fixtures are delegated to
 * {@link ReturnCommandFixtures}.
 */
abstract class AbstractReturnCommandE2ETest extends AbstractPortfolioCalculationE2ETest {

  protected static final SecurityIdentifier XBAL = ReturnCommandFixtures.XBAL;
  protected static final SecurityIdentifier VCNS = ReturnCommandFixtures.VCNS;
  protected static final SecurityIdentifier VTI = ReturnCommandFixtures.VTI;
  protected static final SecurityIdentifier SPY = ReturnCommandFixtures.SPY;
  protected static final SecurityIdentifier F0CAN999 = ReturnCommandFixtures.F0CAN999;
  protected static final SecurityIdentifier CCM4752 = ReturnCommandFixtures.CCM4752;
  protected static final EquitySecurityIdentifier RY_TO = ReturnCommandFixtures.RY_TO;

  protected static ReturnCommand commandFor(CalculationMetric metric, Currency currency,
      List<PortfolioHolding> holdings) {
    return ReturnCommandFixtures.commandFor(metric, currency, holdings);
  }

  protected static MonthlyReturns monthlyReturns(List<DateBigDecimalValue> returns, DataProvider provider,
      String asOf) {
    return ReturnCommandFixtures.monthlyReturns(returns, provider, asOf);
  }

  protected static List<DateBigDecimalValue> returns(String... dateValuePairs) {
    return ReturnCommandFixtures.returns(dateValuePairs);
  }
}
