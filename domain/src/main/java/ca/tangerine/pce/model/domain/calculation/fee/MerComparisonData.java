package ca.tangerine.pce.model.domain.calculation.fee;

import java.util.Map;

import ca.tangerine.pce.model.domain.calculation.PortfolioBenchmarkData;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.security.SecurityData;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;

/**
 * Prepared Market Investment Catalogue fee data for the {@code mer-benchmark-comparison} metric: the portfolio
 * holdings' fee data and the benchmark holdings' fee data, fetched into separate sections of {@code SecurityData}.
 */
public record MerComparisonData(
    Map<PortfolioHolding, FeeData> portfolio,
    Map<PortfolioHolding, FeeData> benchmark) implements PortfolioBenchmarkData<FeeData> {

  public static MerComparisonData from(SecurityData securityData) {
    return new MerComparisonData(
        securityData.get(CompositeSecurityAttribute.FEES),
        securityData.getBenchmark(CompositeSecurityAttribute.FEES));
  }

}
