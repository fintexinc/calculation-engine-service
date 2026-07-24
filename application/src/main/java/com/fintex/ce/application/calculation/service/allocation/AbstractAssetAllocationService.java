package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationData;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.RegionDatapoint;
import com.fintex.wm.commons.domain.allocation.SecurityRegion;
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

import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;

/**
 * Shared implementation for asset-allocation breakdown services, on the {@link AbstractBreakdownService} template. Cash
 * and GIC holdings are first-class buckets here (not excluded): cash maps to {@code CASH}, GIC to its typed region.
 * Stocks resolve via {@link Geography#getRegion()} mapped to {@link AssetAllocationRegionType}, funds via the typed
 * allocations. Weights are normalized to the default target currency configured in
 * {@link FxProperties#getDefaultTargetCurrency()} via {@link DefaultTargetCurrencyConverter}. Before rescaling,
 * near-zero aggregated values (|value| &lt; 1e-5) are clamped to zero — Morningstar reports tiny residual values in
 * buckets like {@code OTHER} or {@code CASH} for derivatives accounting and percentage-rounding offsets, and surfacing
 * them as ~1e-6 noise in user output is confusing while real positions are always orders of magnitude larger.
 * Subclasses may collapse buckets via {@link #collapseBuckets}.
 */
public abstract class AbstractAssetAllocationService<R extends BaseCalculationResult>
    extends
      AbstractBreakdownService<AssetAllocationData, R, AssetAllocationRegionType> {

  private static final BigDecimal NEAR_ZERO_THRESHOLD = new BigDecimal("0.00001");

  protected AbstractAssetAllocationService(PortfolioWeightCalculator portfolioWeightCalculator) {
    super(portfolioWeightCalculator, AssetAllocationRegionType.class);
  }

  @Override
  public List<CompositeSecurityAttribute> requiredAttributes() {
    return List.of(CompositeSecurityAttribute.ASSET_ALLOCATION, CompositeSecurityAttribute.GEOGRAPHY);
  }

  @Override
  public AssetAllocationData prepareData(SecurityData securityData) {
    return new AssetAllocationData(securityData.get(CompositeSecurityAttribute.ASSET_ALLOCATION),
        securityData.get(CompositeSecurityAttribute.GEOGRAPHY));
  }

  @Override
  protected boolean participatesInBreakdown(PortfolioHolding holding) {
    return true;
  }

  @Override
  protected Currency currencyFor(PortfolioHolding holding, AssetAllocationData data) {
    if (CASH_PREDICATE.test(holding)) {
      return ((CashHolding) holding).getCurrency();
    }
    if (GIC_PREDICATE.test(holding)) {
      return ((GicHolding) holding).getCurrency();
    }
    if (STOCK_PREDICATE.test(holding)) {
      return Optional.ofNullable(data.geographies().get(holding))
          .map(Geography::getCurrency)
          .map(CurrencyDatapoint::getValue)
          .orElse(null);
    }
    return Optional.ofNullable(data.allocations().get(holding))
        .map(HoldingAssetAllocation::getCurrency)
        .orElse(null);
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> exposureFor(PortfolioHolding holding, AssetAllocationData data,
      List<Notification> warnings) {
    if (CASH_PREDICATE.test(holding)) {
      return singleBucket(AssetAllocationRegionType.CASH);
    }
    if (GIC_PREDICATE.test(holding)) {
      return singleBucket(((GicHolding) holding).getAssetAllocationRegionType());
    }
    if (STOCK_PREDICATE.test(holding)) {
      return stockAllocation(holding, data.geographies().get(holding), warnings);
    }
    return fundAllocation(holding, data.allocations().get(holding), warnings);
  }

  @Override
  protected final Map<AssetAllocationRegionType, BigDecimal> postProcess(
      Map<AssetAllocationRegionType, BigDecimal> netProducts) {
    return denoise(collapseBuckets(netProducts));
  }

  /**
   * Asset-allocation buckets are absolute portfolio proportions (cash, GIC and each asset class already sum to the
   * whole portfolio), so they are surfaced as-is rather than re-based to 100%.
   */
  @Override
  protected final Map<AssetAllocationRegionType, BigDecimal> normalize(
      Map<AssetAllocationRegionType, BigDecimal> netProducts) {
    return netProducts;
  }

  /**
   * Optional bucket collapse before denoising / rescaling (e.g. folding emerging markets into international equities).
   * Default is identity.
   */
  protected Map<AssetAllocationRegionType, BigDecimal> collapseBuckets(
      Map<AssetAllocationRegionType, BigDecimal> netProducts) {
    return netProducts;
  }

  private Map<AssetAllocationRegionType, BigDecimal> denoise(Map<AssetAllocationRegionType, BigDecimal> netProducts) {
    Map<AssetAllocationRegionType, BigDecimal> denoised = new EnumMap<>(AssetAllocationRegionType.class);
    for (Map.Entry<AssetAllocationRegionType, BigDecimal> entry : netProducts.entrySet()) {
      BigDecimal value = entry.getValue();
      denoised.put(entry.getKey(),
          value == null || value.abs().compareTo(NEAR_ZERO_THRESHOLD) < 0 ? BigDecimal.ZERO : value);
    }
    return denoised;
  }

  private Map<AssetAllocationRegionType, BigDecimal> stockAllocation(PortfolioHolding holding, Geography geography,
      List<Notification> warnings) {
    if (geography == null) {
      warnings.add(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC.toNotificationForHolding(holding,
          getMetric().getUserFriendlyName()));
      return singleBucket(AssetAllocationRegionType.UNCLASSIFIED);
    }
    SecurityRegion region = Optional.ofNullable(geography.getRegion())
        .map(RegionDatapoint::getValue)
        .orElse(null);
    if (region == null) {
      warnings.add(ErrorCode.MISSING_BUSINESS_COUNTRY_CODE.toNotificationForHolding(holding));
      return singleBucket(AssetAllocationRegionType.UNCLASSIFIED);
    }
    return singleBucket(equityTypeFor(region));
  }

  private AssetAllocationRegionType equityTypeFor(SecurityRegion region) {
    for (AssetAllocationRegionType type : AssetAllocationRegionType.values()) {
      if (region.equals(type.getSecurityRegion())) {
        return type;
      }
    }
    return AssetAllocationRegionType.INTERNATIONAL_EQUITIES;
  }

  private Map<AssetAllocationRegionType, BigDecimal> fundAllocation(PortfolioHolding holding,
      HoldingAssetAllocation allocation, List<Notification> warnings) {
    if (allocation == null) {
      warnings.add(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC.toNotificationForHolding(holding,
          getMetric().getUserFriendlyName()));
      return singleBucket(AssetAllocationRegionType.UNCLASSIFIED);
    }
    if (allocation.getAllocations() == null || allocation.getAllocations().isEmpty()) {
      warnings.add(ErrorCode.MISSING_ASSET_ALLOCATION.toNotificationForHolding(holding));
      return singleBucket(AssetAllocationRegionType.UNCLASSIFIED);
    }
    return new EnumMap<>(allocation.getAllocations());
  }
}
