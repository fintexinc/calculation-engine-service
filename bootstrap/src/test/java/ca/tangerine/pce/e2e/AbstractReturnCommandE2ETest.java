package ca.tangerine.pce.e2e;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.ReturnCommand;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.id.EquitySecurityIdentifier;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;
import ca.tangerine.wm.commons.domain.performance.MonthlyReturns;
import ca.tangerine.wm.commons.domain.value.DateBigDecimalValue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared e2e infrastructure for {@link ReturnCommand}-based metrics (growth-of-10k, annual-returns). Both metrics use
 * the same command type, the same Market Investment Catalogue call
 * ({@code List<SecurityAttributeResult<MonthlyReturns>>}) and the same holding shapes, so their fixtures live here
 * rather than being duplicated per metric. Subclasses supply the metric-specific request bodies, MIC responses and
 * assertions required by {@link AbstractPortfolioCalculationE2ETest}.
 */
abstract class AbstractReturnCommandE2ETest extends AbstractPortfolioCalculationE2ETest {

  protected static final SecurityIdentifier XBAL = new SecurityIdentifier("XBAL", FiIdentifierType.TICKER);
  protected static final SecurityIdentifier VCNS = new SecurityIdentifier("VCNS", FiIdentifierType.TICKER);
  protected static final SecurityIdentifier VTI = new SecurityIdentifier("VTI", FiIdentifierType.TICKER);
  protected static final SecurityIdentifier SPY = new SecurityIdentifier("SPY", FiIdentifierType.TICKER);
  protected static final SecurityIdentifier F0CAN999 = new SecurityIdentifier("F0CAN999",
      FiIdentifierType.MORNINGSTAR_ID);
  protected static final SecurityIdentifier CCM4752 = new SecurityIdentifier("CCM4752", FiIdentifierType.FUNDSERV);
  protected static final EquitySecurityIdentifier RY_TO = EquitySecurityIdentifier.builder()
      .id("RY.TO")
      .idType(FiIdentifierType.TICKER_MIC)
      .exchangeId("TSX")
      .build();

  protected static ReturnCommand commandFor(CalculationMetric metric, Currency currency,
      List<PortfolioHolding> holdings) {
    ReturnCommand command = new ReturnCommand();
    command.setMetric(metric);
    command.setCurrency(currency);
    command.setHoldings(holdings);
    return command;
  }

  protected static MonthlyReturns monthlyReturns(List<DateBigDecimalValue> returns, DataProvider provider,
      String asOf) {
    MonthlyReturns monthlyReturns = new MonthlyReturns();
    monthlyReturns.setReturns(returns);
    monthlyReturns.setDataProviders(provider == null ? null : List.of(provider));
    monthlyReturns.setAsOfDate(LocalDateTime.parse(asOf));
    return monthlyReturns;
  }

  protected static List<DateBigDecimalValue> returns(String... dateValuePairs) {
    if (dateValuePairs.length % 2 != 0) {
      throw new IllegalArgumentException("expected even number of strings: date,value pairs");
    }
    var list = new ArrayList<DateBigDecimalValue>(dateValuePairs.length / 2);
    for (int i = 0; i < dateValuePairs.length; i += 2) {
      list.add(new DateBigDecimalValue(dateValuePairs[i], new BigDecimal(dateValuePairs[i + 1])));
    }
    return list;
  }
}
