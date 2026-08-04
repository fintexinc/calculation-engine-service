package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.ConsolidatedGeographicExposureResult;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;

/**
 * Consolidated geographic exposure: the region distribution of the portfolio <em>as a whole</em>, equity and fixed
 * income together.
 *
 * <p>
 * <b>Why this metric exists rather than summing the two per-sleeve ones.</b> {@code equity-geographic-exposure} and
 * {@code fixed-income-geographic-exposure} each weight against their own sleeve, and a balanced fund participates in
 * both at its full portfolio weight — adding them double-counts it. Only a metric that derives one distribution from a
 * single whole-security datapoint answers "where is my money invested".
 *
 * <p>
 * <b>Why the source is {@code GEOGRAPHIC_ALLOCATION}.</b> Security Master buckets the vendor's whole-security country
 * exposure into regions at ingest, the same mapping it applies to the two sleeve breakdowns. Rolling the country
 * datapoint up here instead would not give the same answer: Security Master keeps a country label the catalog cannot
 * resolve by bucketing it into {@link GeographicRegionType#OTHER}, whereas {@code COUNTRY_ALLOCATION} carries such an
 * entry with a {@code null} type, which the mapper drops — so a downstream rollup silently loses that exposure and
 * renormalizes the remaining regions over it. Supranational issuers alone (present in ~3000 of the 4796 balanced
 * securities in the provider extract) make that a routine loss rather than an edge case.
 *
 * <p>
 * <b>Why the denominator is every holding but cash and GIC.</b> Unlike the two per-sleeve services this metric must not
 * scope to a sleeve — the whole portfolio is the point. Cash and GIC carry no geography, so naming them here keeps the
 * exposure set and the weighting denominator in step; it is also arithmetically identical to leaving them in the
 * denominator, because {@code AbstractBreakdownService} normalizes with {@code reScale}, which divides by the summed
 * net products, so scaling every weight by the same constant cancels. Either way the reported regions total 100% while
 * the Asset Mix donut, which does carry a cash slice, is shown separately — which is what the product spec asks for.
 */
@Service
public class GeographicExposureService
    extends
      AbstractGeographicExposureService<ConsolidatedGeographicExposureResult> {

  private static final Predicate<PortfolioHolding> GEOGRAPHY_BEARING = CASH_PREDICATE.or(GIC_PREDICATE).negate();

  public GeographicExposureService(PortfolioWeightCalculator portfolioWeightCalculator,
      StockGeographyRegionResolver stockRegionResolver) {
    super(portfolioWeightCalculator, stockRegionResolver);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.GEOGRAPHIC_EXPOSURE;
  }

  @Override
  protected CompositeSecurityAttribute geographicAllocationAttribute() {
    return CompositeSecurityAttribute.GEOGRAPHIC_ALLOCATION;
  }

  @Override
  protected Predicate<PortfolioHolding> relevantHoldingPredicate() {
    return GEOGRAPHY_BEARING;
  }

  @Override
  protected ErrorCode missingFundAllocationErrorCode() {
    return ErrorCode.MISSING_GEOGRAPHIC_EXPOSURE;
  }

  @Override
  protected ConsolidatedGeographicExposureResult buildResult(Map<GeographicRegionType, BigDecimal> regionMap,
      List<Notification> warnings) {
    ConsolidatedGeographicExposureResult result = new ConsolidatedGeographicExposureResult();
    result.setGeographicExposure(regionMap);
    result.setWarnings(warnings);
    return result;
  }
}
