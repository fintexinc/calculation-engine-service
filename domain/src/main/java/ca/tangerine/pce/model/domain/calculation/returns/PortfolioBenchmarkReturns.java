package ca.tangerine.pce.model.domain.calculation.returns;

import java.util.Map;

import ca.tangerine.pce.model.domain.calculation.PortfolioBenchmarkData;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.security.SecurityData;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;

/**
 * Per-holding return series consumed by the returns-based calculation services.
 */
public record PortfolioBenchmarkReturns(
    Map<PortfolioHolding, HoldingMonthlyReturns> portfolio,
    Map<PortfolioHolding, HoldingMonthlyReturns> benchmark)
    implements
      PortfolioBenchmarkData<HoldingMonthlyReturns> {

  public static final PortfolioBenchmarkReturns EMPTY = new PortfolioBenchmarkReturns(Map.of(), Map.of());

  public static PortfolioBenchmarkReturns from(SecurityData securityData) {
    return new PortfolioBenchmarkReturns(
        securityData.get(CompositeSecurityAttribute.MONTHLY_RETURNS),
        securityData.getBenchmark(CompositeSecurityAttribute.MONTHLY_RETURNS));
  }

}
