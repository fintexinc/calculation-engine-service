package ca.tangerine.pce.application.calculation.service.allocation;

import ca.tangerine.pce.application.calculation.service.PortfolioWeightCalculator;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.exposure.EquityGeographicExposureResult;
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
import static ca.tangerine.pce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static ca.tangerine.pce.util.FilterUtils.GIC_PREDICATE;

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

  public EquityGeographicExposureService(PortfolioWeightCalculator portfolioWeightCalculator,
      StockGeographyRegionResolver stockRegionResolver) {
    super(portfolioWeightCalculator, stockRegionResolver);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_GEOGRAPHIC_EXPOSURE;
  }

  @Override
  protected CompositeSecurityAttribute geographicAllocationAttribute() {
    return CompositeSecurityAttribute.EQUITY_GEOGRAPHIC_ALLOCATION;
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
