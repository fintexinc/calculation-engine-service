package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.RegionDatapoint;
import com.fintex.wm.commons.domain.allocation.SecurityRegion;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fintex.ce.application.util.PortfolioUtils.calculateInitialPortfolioWeight;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

/**
 * Shared implementation for asset-allocation breakdown services. Resolves each holding's region — stocks via
 * {@link Geography#getRegion()} mapped directly to {@link AssetAllocationRegionType} through
 * {@link AssetAllocationRegionType#getSecurityRegion()}, funds via the typed allocations from SMS — and aggregates
 * region exposures using portfolio weights derived from holding market values normalized to the default target currency
 * configured in {@link FxProperties#getDefaultTargetCurrency()}. Currency conversion is delegated to
 * {@link DefaultTargetCurrencyConverter}, which fetches spot FX rates and reports FX-rate-unavailable warnings. When a
 * holding has no source currency, its raw value participates in the weight unchanged (no warning). Subclasses produce
 * the result object and may post-process the aggregated map (e.g., collapse buckets). Before scaling to user output,
 * near-zero aggregated values (|value| &lt; 1e-5) are clamped to zero — Morningstar reports tiny residual values in
 * buckets like {@code OTHER} or {@code CASH} for derivatives accounting and percentage-rounding offsets, and surfacing
 * them as ~1e-6 noise in user output is confusing while real positions are always orders of magnitude larger than the
 * threshold.
 */
public abstract class AbstractAssetAllocationService<R extends BaseCalculationResult>
    extends
      BreakdownAbstractService<R, AssetAllocationRegionType> {

  private static final BigDecimal NEAR_ZERO_THRESHOLD = new BigDecimal("0.00001");

  protected final SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher;
  protected final SecurityDataFetcher<Geography> geographyFetcher;
  protected final PortfolioWeightCalculator portfolioWeightCalculator;
  protected final DefaultDataProperties defaultDataProperties;

  protected AbstractAssetAllocationService(SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher,
      SecurityDataFetcher<Geography> geographyFetcher, PortfolioWeightCalculator portfolioWeightCalculator,
      DefaultDataProperties defaultDataProperties) {
    this.assetAllocationFetcher = assetAllocationFetcher;
    this.geographyFetcher = geographyFetcher;
    this.portfolioWeightCalculator = portfolioWeightCalculator;
    this.defaultDataProperties = defaultDataProperties;
  }

  @Override
  public R perform(PortfolioHoldingsCommand command) {
    List<PortfolioHolding> holdings = command.getHoldings();
    List<DataProvider> providers = getSpecifiedIfEmpty(command.getDataProviders(),
        defaultDataProperties.getDataProviders());
    List<Notification> warnings = new ArrayList<>();

    Map<PortfolioHolding, Geography> stockGeographies = geographyFetcher.fetch(
        holdings.stream().filter(STOCK_PREDICATE).toList(), providers);
    Map<PortfolioHolding, HoldingAssetAllocation> fundAllocations = assetAllocationFetcher.fetch(
        holdings.stream().filter(STOCK_PREDICATE.or(CASH_PREDICATE).or(GIC_PREDICATE).negate()).toList(),
        providers);

    Map<PortfolioHolding, Map<AssetAllocationRegionType, BigDecimal>> exposures = new HashMap<>();
    Map<PortfolioHolding, Currency> currencies = new HashMap<>();
    for (PortfolioHolding holding : holdings) {
      exposures.put(holding, allocationFor(holding, fundAllocations, stockGeographies, warnings));
      Currency currency = currencyFor(holding, fundAllocations, stockGeographies);
      if (currency != null) {
        currencies.put(holding, currency);
      }
    }

    PortfolioWeightCalculator.Result weightResult = portfolioWeightCalculator.compute(holdings, currencies);
    warnings.addAll(weightResult.warnings());
    Map<AssetAllocationRegionType, BigDecimal> netProducts = aggregateWith(exposures, weightResult.weights());
    postProcess(netProducts);
    return buildResult(netProducts, warnings);
  }

  @Override
  public ExposureDataHolder<AssetAllocationRegionType> fetchExposures(PortfolioHoldingsCommand command) {
    List<PortfolioHolding> holdings = command.getHoldings();
    List<DataProvider> providers = getSpecifiedIfEmpty(command.getDataProviders(),
        defaultDataProperties.getDataProviders());
    List<Notification> warnings = new ArrayList<>();

    Map<PortfolioHolding, Geography> stockGeographies = geographyFetcher.fetch(
        holdings.stream().filter(STOCK_PREDICATE).toList(), providers);
    Map<PortfolioHolding, HoldingAssetAllocation> fundAllocations = assetAllocationFetcher.fetch(
        holdings.stream().filter(STOCK_PREDICATE.or(CASH_PREDICATE).or(GIC_PREDICATE).negate()).toList(),
        providers);

    Map<PortfolioHolding, Map<AssetAllocationRegionType, BigDecimal>> exposures = new HashMap<>();
    for (PortfolioHolding holding : holdings) {
      exposures.put(holding, allocationFor(holding, fundAllocations, stockGeographies, warnings));
    }
    return new ExposureDataHolder<>(exposures, warnings);
  }

  /**
   * Aggregates pre-fetched exposures using raw holding values as weights, without applying FX normalization. This
   * diverges from {@link #perform} for multi-currency portfolios: {@code perform} converts each holding's value to the
   * default target currency (see {@link FxProperties#getDefaultTargetCurrency()}) via
   * {@link DefaultTargetCurrencyConverter} before weighting, while this method weights by raw values in the holding's
   * own currency. The discrepancy is silent and proportional to the spread between holding currencies and the target.
   * <p>
   * Use this entry point only when (a) all holdings are already in the default target currency, (b) the caller has
   * pre-normalized values to a single currency, or (c) the test isolates a single bucket and currency does not affect
   * the assertion. For multi-currency production portfolios, call {@link #perform} instead.
   */
  @Override
  public R calculate(ExposureDataHolder<AssetAllocationRegionType> exposureData, List<PortfolioHolding> holdings) {
    Map<PortfolioHolding, BigDecimal> weights = calculateInitialPortfolioWeight(holdings);
    Map<AssetAllocationRegionType, BigDecimal> netProducts = aggregateWith(exposureData.allocations(), weights);
    postProcess(netProducts);
    return buildResult(netProducts, new ArrayList<>(exposureData.warnings()));
  }

  protected abstract R buildResult(Map<AssetAllocationRegionType, BigDecimal> netProducts,
      List<Notification> warnings);

  protected void postProcess(Map<AssetAllocationRegionType, BigDecimal> netProducts) {
  }

  protected Map<AssetAllocationRegionType, BigDecimal> toUserScale(
      Map<AssetAllocationRegionType, BigDecimal> netProducts) {
    Map<AssetAllocationRegionType, BigDecimal> denoised = new EnumMap<>(AssetAllocationRegionType.class);
    for (Map.Entry<AssetAllocationRegionType, BigDecimal> entry : netProducts.entrySet()) {
      BigDecimal value = entry.getValue();
      if (value == null || value.abs().compareTo(NEAR_ZERO_THRESHOLD) < 0) {
        denoised.put(entry.getKey(), BigDecimal.ZERO);
      } else {
        denoised.put(entry.getKey(), value);
      }
    }
    return DecimalUtils.toUserScale(denoised);
  }

  private Map<AssetAllocationRegionType, BigDecimal> aggregateWith(
      Map<PortfolioHolding, Map<AssetAllocationRegionType, BigDecimal>> exposures,
      Map<PortfolioHolding, BigDecimal> weights) {
    Map<AssetAllocationRegionType, BigDecimal> netProducts = new EnumMap<>(AssetAllocationRegionType.class);
    for (AssetAllocationRegionType type : AssetAllocationRegionType.values()) {
      netProducts.put(type, calculateNetProduct(type, exposures, weights));
    }
    return netProducts;
  }

  private Map<AssetAllocationRegionType, BigDecimal> allocationFor(PortfolioHolding holding,
      Map<PortfolioHolding, HoldingAssetAllocation> fundAllocations,
      Map<PortfolioHolding, Geography> stockGeographies,
      List<Notification> warnings) {
    if (CASH_PREDICATE.test(holding)) {
      return singleRegion(AssetAllocationRegionType.CASH);
    }
    if (GIC_PREDICATE.test(holding)) {
      return singleRegion(((GicHolding) holding).getAssetAllocationRegionType());
    }
    if (STOCK_PREDICATE.test(holding)) {
      return stockAllocation(holding, stockGeographies.get(holding), warnings);
    }
    return fundAllocation(holding, fundAllocations.get(holding), warnings);
  }

  private Currency currencyFor(PortfolioHolding holding,
      Map<PortfolioHolding, HoldingAssetAllocation> fundAllocations,
      Map<PortfolioHolding, Geography> stockGeographies) {
    if (CASH_PREDICATE.test(holding)) {
      return ((CashHolding) holding).getCurrency();
    }
    if (GIC_PREDICATE.test(holding)) {
      return ((GicHolding) holding).getCurrency();
    }
    if (STOCK_PREDICATE.test(holding)) {
      return Optional.ofNullable(stockGeographies.get(holding))
          .map(Geography::getCurrency)
          .map(CurrencyDatapoint::getValue)
          .orElse(null);
    }
    return Optional.ofNullable(fundAllocations.get(holding))
        .map(HoldingAssetAllocation::getCurrency)
        .orElse(null);
  }

  private Map<AssetAllocationRegionType, BigDecimal> stockAllocation(PortfolioHolding holding, Geography geography,
      List<Notification> warnings) {
    if (geography == null) {
      warnings.add(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC.toNotificationForHolding(holding,
          getMetric().getUserFriendlyName()));
      return singleRegion(AssetAllocationRegionType.UNCLASSIFIED);
    }
    SecurityRegion region = Optional.ofNullable(geography.getRegion())
        .map(RegionDatapoint::getValue)
        .orElse(null);
    if (region == null) {
      warnings.add(ErrorCode.MISSING_BUSINESS_COUNTRY_CODE.toNotificationForHolding(holding));
      return singleRegion(AssetAllocationRegionType.UNCLASSIFIED);
    }
    return singleRegion(equityTypeFor(region));
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
      return singleRegion(AssetAllocationRegionType.UNCLASSIFIED);
    }
    if (allocation.getAllocations() == null || allocation.getAllocations().isEmpty()) {
      warnings.add(ErrorCode.MISSING_ASSET_ALLOCATION.toNotificationForHolding(holding));
      return singleRegion(AssetAllocationRegionType.UNCLASSIFIED);
    }
    return new EnumMap<>(allocation.getAllocations());
  }

  private Map<AssetAllocationRegionType, BigDecimal> singleRegion(AssetAllocationRegionType type) {
    Map<AssetAllocationRegionType, BigDecimal> result = new EnumMap<>(AssetAllocationRegionType.class);
    result.put(type, BigDecimal.ONE);
    return result;
  }

}
