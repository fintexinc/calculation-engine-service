package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.model.domain.calculation.allocation.GeographicExposureData;
import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.GeographicExposureResult;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;

/**
 * Shared base for the geographic exposure services, on the {@link AbstractBreakdownService} template. Resolves each
 * relevant holding's per-region exposure from the correct Market Investment Catalogue endpoint: stocks via
 * {@link Geography#getBusinessCountry()} (Market Investment Catalogue does not publish a per-bucket
 * geographic-allocation breakdown for individual stocks — only one region applies, so a single 100% bucket is emitted),
 * funds / ETFs / bonds via the typed {@link HoldingGeographicAllocation}. Currency for stocks comes from
 * {@link Geography#getCurrency()}, for funds from {@link HoldingGeographicAllocation#getCurrency()}.
 *
 * <p>
 * Each subclass picks its region-allocation attribute — equity sleeve, fixed-income sleeve or whole security — and
 * which holdings participate via {@link #relevantHoldingPredicate()}, so values from non-applicable holdings do not
 * pollute the denominator: the two per-sleeve services exclude the opposite sleeve, while the consolidated
 * {@link GeographicExposureService} excludes only the holdings that carry no geography at all.
 *
 * @param <R>
 *          concrete result type (equity, fixed income or consolidated)
 */
public abstract class AbstractGeographicExposureService<R extends GeographicExposureResult>
    extends
      AbstractBreakdownService<GeographicExposureData, R, GeographicRegionType> {

  private final StockGeographyRegionResolver stockRegionResolver;

  protected AbstractGeographicExposureService(PortfolioWeightCalculator portfolioWeightCalculator,
      StockGeographyRegionResolver stockRegionResolver) {
    super(portfolioWeightCalculator, GeographicRegionType.class);
    this.stockRegionResolver = stockRegionResolver;
  }

  @Override
  public List<CompositeSecurityAttribute> requiredAttributes() {
    return List.of(geographicAllocationAttribute(), CompositeSecurityAttribute.GEOGRAPHY);
  }

  @Override
  public GeographicExposureData prepareData(SecurityData securityData) {
    return new GeographicExposureData(securityData.get(geographicAllocationAttribute()),
        securityData.get(CompositeSecurityAttribute.GEOGRAPHY));
  }

  @Override
  protected boolean participatesInBreakdown(PortfolioHolding holding) {
    return relevantHoldingPredicate().test(holding);
  }

  @Override
  protected List<PortfolioHolding> weightingHoldings(List<PortfolioHolding> holdings) {
    return holdings.stream().filter(relevantHoldingPredicate()).toList();
  }

  @Override
  protected Currency currencyFor(PortfolioHolding holding, GeographicExposureData data) {
    if (STOCK_PREDICATE.test(holding)) {
      return Optional.ofNullable(data.geographies().get(holding))
          .map(Geography::getCurrency)
          .map(CurrencyDatapoint::getValue)
          .orElse(null);
    }
    return Optional.ofNullable(data.allocations().get(holding))
        .map(HoldingGeographicAllocation::getCurrency)
        .orElse(null);
  }

  @Override
  protected Map<GeographicRegionType, BigDecimal> exposureFor(PortfolioHolding holding, GeographicExposureData data,
      List<Notification> warnings) {
    if (STOCK_PREDICATE.test(holding)) {
      return stockAllocation(holding, data.geographies().get(holding), warnings);
    }
    return fundAllocation(holding, data.allocations().get(holding), warnings);
  }

  protected abstract CompositeSecurityAttribute geographicAllocationAttribute();

  protected abstract Predicate<PortfolioHolding> relevantHoldingPredicate();

  protected abstract ErrorCode missingFundAllocationErrorCode();

  private Map<GeographicRegionType, BigDecimal> stockAllocation(PortfolioHolding holding, Geography geography,
      List<Notification> warnings) {
    GeographicRegionType region = stockRegionResolver.resolve(holding, geography, getMetric().getUserFriendlyName(),
        warnings);
    return singleBucket(region == null ? GeographicRegionType.UNKNOWN : region);
  }

  private Map<GeographicRegionType, BigDecimal> fundAllocation(PortfolioHolding holding,
      HoldingGeographicAllocation allocation, List<Notification> warnings) {
    if (allocation == null) {
      warnings.add(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC.toNotificationForHolding(holding,
          getMetric().getUserFriendlyName()));
      return singleBucket(GeographicRegionType.UNKNOWN);
    }
    if (allocation.getAllocations() == null || allocation.getAllocations().isEmpty()) {
      warnings.add(missingFundAllocationErrorCode().toNotificationForHolding(holding));
      return singleBucket(GeographicRegionType.UNKNOWN);
    }
    return new EnumMap<>(allocation.getAllocations());
  }
}
