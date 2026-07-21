package com.fintex.ce.model.domain.calculation.returns;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;

import java.util.Map;

/**
 * Per-holding return series consumed by the returns-based calculation services, with the portfolio and benchmark sides
 * kept in separate maps — the same security may appear in both holdings lists, so the two sides must never share one
 * lookup table. Portfolio-only services simply ignore {@code benchmarkReturns}.
 */
public record PortfolioBenchmarkReturns(
    Map<PortfolioHolding, HoldingMonthlyReturns> portfolioReturns,
    Map<PortfolioHolding, HoldingMonthlyReturns> benchmarkReturns) {

  public static final PortfolioBenchmarkReturns EMPTY = new PortfolioBenchmarkReturns(Map.of(), Map.of());

  public static PortfolioBenchmarkReturns from(SecurityData securityData) {
    return new PortfolioBenchmarkReturns(
        securityData.get(CompositeSecurityAttribute.MONTHLY_RETURNS),
        securityData.getBenchmark(CompositeSecurityAttribute.MONTHLY_RETURNS));
  }

}
