package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.FixedIncomeGeographicExposureResult;
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
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;

/**
 * Fixed-income geographic exposure breakdown service. Aggregates per-security FI geographic allocations into the
 * portfolio-level distribution using currency-adjusted weights; excludes cash, GIC and pure stock holdings from both
 * the fetch and the weight denominator so the breakdown reflects the bond-bearing portion of the portfolio.
 */
@Service
public class FixedIncomeGeographicExposureService
    extends
      AbstractGeographicExposureService<FixedIncomeGeographicExposureResult> {

  private static final Predicate<PortfolioHolding> BOND_BEARING = CASH_PREDICATE
      .or(GIC_PREDICATE)
      .or(STOCK_PREDICATE)
      .negate();

  public FixedIncomeGeographicExposureService(
      @Qualifier("fixedIncomeGeographicAllocationFetcher") SecurityDataFetcher<HoldingGeographicAllocation> fixedIncomeGeographicAllocationFetcher,
      SecurityDataFetcher<Geography> geographyFetcher,
      PortfolioWeightCalculator portfolioWeightCalculator,
      DefaultDataProperties defaultDataProperties) {
    super(fixedIncomeGeographicAllocationFetcher, geographyFetcher, portfolioWeightCalculator, defaultDataProperties);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_GEOGRAPHIC_EXPOSURE;
  }

  @Override
  protected Predicate<PortfolioHolding> relevantHoldingPredicate() {
    return BOND_BEARING;
  }

  @Override
  protected ErrorCode missingFundAllocationErrorCode() {
    return ErrorCode.MISSING_FIXED_INCOME_GEOGRAPHIC_EXPOSURE;
  }

  @Override
  protected FixedIncomeGeographicExposureResult buildResult(Map<GeographicRegionType, BigDecimal> regionMap,
      List<Notification> warnings) {
    FixedIncomeGeographicExposureResult result = new FixedIncomeGeographicExposureResult();
    result.setGeographicExposure(regionMap);
    result.setWarnings(warnings);
    return result;
  }
}
