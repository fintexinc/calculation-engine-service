package com.fintex.ce.model.domain.calculation.fee;

import com.fintex.ce.model.domain.calculation.PortfolioBenchmarkData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;

import java.util.Map;

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
