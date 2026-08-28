package ca.tangerine.pce.application.calculation.service.allocation;

import ca.tangerine.pce.application.calculation.service.PortfolioWeightCalculator;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.exposure.FixedIncomeGeographicExposureResult;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.allocation.GeographicRegionType;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static ca.tangerine.pce.util.FilterUtils.CASH_PREDICATE;
import static ca.tangerine.pce.util.FilterUtils.GIC_PREDICATE;
import static ca.tangerine.pce.util.FilterUtils.STOCK_PREDICATE;

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

  public FixedIncomeGeographicExposureService(PortfolioWeightCalculator portfolioWeightCalculator,
      StockGeographyRegionResolver stockRegionResolver) {
    super(portfolioWeightCalculator, stockRegionResolver);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_GEOGRAPHIC_EXPOSURE;
  }

  @Override
  protected CompositeSecurityAttribute geographicAllocationAttribute() {
    return CompositeSecurityAttribute.FIXED_INCOME_GEOGRAPHIC_ALLOCATION;
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
