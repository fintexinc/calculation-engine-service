package com.fintex.ce.model.domain.calculation.returns;

import com.fintex.ce.model.domain.calculation.PortfolioBenchmarkData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;

import java.util.Map;

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
