package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.EquityGeographicExposureResult;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.error.Notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;

/**
 * Equity geographic exposure breakdown service. Aggregates per-security equity geographic allocations into the
 * portfolio-level distribution using currency-adjusted weights; excludes cash, GIC and pure fixed-income holdings from
 * both the fetch and the weight denominator so the breakdown reflects the equity-bearing portion of the portfolio.
 */
@Service
public class EquityGeographicExposureService
    extends
      AbstractGeographicExposureService<EquityGeographicExposureResult> {

  private static final Predicate<PortfolioHolding> EQUITY_BEARING = CASH_PREDICATE
      .or(GIC_PREDICATE)
      .or(FIXED_INCOME_PREDICATE)
      .negate();

  public EquityGeographicExposureService(
      @Qualifier("equityGeographicAllocationFetcher") SecurityDataFetcher<HoldingGeographicAllocation> equityGeographicAllocationFetcher,
      SecurityDataFetcher<Geography> geographyFetcher,
      PortfolioWeightCalculator portfolioWeightCalculator,
      DefaultDataProperties defaultDataProperties) {
    super(equityGeographicAllocationFetcher, geographyFetcher, portfolioWeightCalculator, defaultDataProperties);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_GEOGRAPHIC_EXPOSURE;
  }

  @Override
  protected Predicate<PortfolioHolding> relevantHoldingPredicate() {
    return EQUITY_BEARING;
  }

  @Override
  protected ErrorCode missingFundAllocationErrorCode() {
    return ErrorCode.MISSING_EQUITY_GEOGRAPHIC_EXPOSURE;
  }

  @Override
  protected EquityGeographicExposureResult buildResult(Map<GeographicRegionType, BigDecimal> regionMap,
      List<Notification> warnings) {
    EquityGeographicExposureResult result = new EquityGeographicExposureResult();
    result.setGeographicExposure(regionMap);
    result.setWarnings(warnings);
    return result;
  }
}
