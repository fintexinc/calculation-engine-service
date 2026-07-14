package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.GeographicExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.allocation.RegionDatapoint;
import com.fintex.wm.commons.domain.allocation.SecurityRegion;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.domain.reference.CountryDatapoint;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.fintex.ce.application.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.application.util.CalculationUtils.sumProduct;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

/**
 * Shared base for the equity / fixed-income geographic exposure services. Resolves each relevant holding's per-region
 * exposure from the correct Security Master endpoint: stocks via {@link Geography#getBusinessCountry()} (Security
 * Master does not publish a per-bucket geographic-allocation breakdown for individual stocks — only one region applies,
 * so a single 100% bucket is emitted), funds / ETFs / bonds via the typed {@link HoldingGeographicAllocation} from SMS.
 * Currency for stocks is sourced from {@link Geography#getCurrency()}, for funds from
 * {@link HoldingGeographicAllocation#getCurrency()}. The aggregated exposures are then rolled up to the portfolio using
 * currency-adjusted weights so multi-currency portfolios produce a correct distribution. Each subclass picks which
 * holdings participate in the breakdown via {@link #relevantHoldingPredicate()} — equity services exclude bond-only
 * holdings and vice versa — so values from non-applicable holdings do not pollute the denominator.
 *
 * @param <R>
 *          concrete result type (equity or fixed income)
 */
public abstract class AbstractGeographicExposureService<R extends GeographicExposureResult>
    implements
      CalculationService<PortfolioHoldingsCommand, R> {

  protected final SecurityDataFetcher<HoldingGeographicAllocation> geographicAllocationFetcher;
  protected final SecurityDataFetcher<Geography> geographyFetcher;
  protected final PortfolioWeightCalculator portfolioWeightCalculator;
  protected final DefaultDataProperties defaultDataProperties;

  protected AbstractGeographicExposureService(
      SecurityDataFetcher<HoldingGeographicAllocation> geographicAllocationFetcher,
      SecurityDataFetcher<Geography> geographyFetcher,
      PortfolioWeightCalculator portfolioWeightCalculator,
      DefaultDataProperties defaultDataProperties) {
    this.geographicAllocationFetcher = geographicAllocationFetcher;
    this.geographyFetcher = geographyFetcher;
    this.portfolioWeightCalculator = portfolioWeightCalculator;
    this.defaultDataProperties = defaultDataProperties;
  }

  @Override
  public R perform(PortfolioHoldingsCommand command) {
    List<PortfolioHolding> allHoldings = command.getHoldings();
    List<DataProvider> providers = getSpecifiedIfEmpty(command.getDataProviders(),
        defaultDataProperties.getDataProviders());
    List<Notification> warnings = new ArrayList<>();

    List<PortfolioHolding> relevant = allHoldings.stream().filter(relevantHoldingPredicate()).toList();
    if (relevant.isEmpty()) {
      return buildResult(emptyRegionMap(), warnings);
    }

    List<PortfolioHolding> stocks = relevant.stream().filter(STOCK_PREDICATE).toList();
    List<PortfolioHolding> nonStocks = relevant.stream().filter(STOCK_PREDICATE.negate()).toList();

    Map<PortfolioHolding, Geography> stockGeographies = geographyFetcher.fetch(stocks, providers);
    Map<PortfolioHolding, HoldingGeographicAllocation> fundExposures = geographicAllocationFetcher.fetch(nonStocks,
        providers);

    Map<PortfolioHolding, Map<GeographicRegionType, BigDecimal>> exposures = new HashMap<>();
    Map<PortfolioHolding, Currency> currencies = new HashMap<>();
    for (PortfolioHolding holding : relevant) {
      exposures.put(holding, allocationFor(holding, stockGeographies, fundExposures, warnings));
      Currency currency = currencyFor(holding, stockGeographies, fundExposures);
      if (currency != null) {
        currencies.put(holding, currency);
      }
    }

    PortfolioWeightCalculator.Result weightResult = portfolioWeightCalculator.compute(relevant, currencies);
    warnings.addAll(weightResult.warnings());

    Map<GeographicRegionType, BigDecimal> netProducts = aggregate(exposures, weightResult.weights());
    return buildResult(toUserScale(reScaleAbs(netProducts)), warnings);
  }

  protected abstract Predicate<PortfolioHolding> relevantHoldingPredicate();

  protected abstract R buildResult(Map<GeographicRegionType, BigDecimal> regionMap, List<Notification> warnings);

  protected abstract ErrorCode missingFundAllocationErrorCode();

  private Map<GeographicRegionType, BigDecimal> allocationFor(PortfolioHolding holding,
      Map<PortfolioHolding, Geography> stockGeographies,
      Map<PortfolioHolding, HoldingGeographicAllocation> fundExposures,
      List<Notification> warnings) {
    if (STOCK_PREDICATE.test(holding)) {
      return stockAllocation(holding, stockGeographies.get(holding), warnings);
    }
    return fundAllocation(holding, fundExposures.get(holding), warnings);
  }

  private Map<GeographicRegionType, BigDecimal> stockAllocation(PortfolioHolding holding, Geography geography,
      List<Notification> warnings) {
    if (geography == null) {
      warnings.add(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC.toNotificationForHolding(holding,
          getMetric().getUserFriendlyName()));
      return unknownAllocation();
    }
    GeographicRegionType region = resolveStockRegion(geography);
    if (region == null) {
      warnings.add(ErrorCode.MISSING_BUSINESS_COUNTRY_CODE.toNotificationForHolding(holding));
      return unknownAllocation();
    }
    Map<GeographicRegionType, BigDecimal> result = new EnumMap<>(GeographicRegionType.class);
    result.put(region, BigDecimal.ONE);
    return result;
  }

  private GeographicRegionType resolveStockRegion(Geography geography) {
    GeographicRegionType fromCountry = Optional.ofNullable(geography)
        .map(Geography::getBusinessCountry)
        .map(CountryDatapoint::getValue)
        .map(Country::getGeographyRegion)
        .orElse(null);
    if (fromCountry != null) {
      return fromCountry;
    }
    SecurityRegion fallbackRegion = Optional.ofNullable(geography)
        .map(Geography::getRegion)
        .map(RegionDatapoint::getValue)
        .orElse(null);
    return regionFromSecurityRegion(fallbackRegion);
  }

  private GeographicRegionType regionFromSecurityRegion(SecurityRegion securityRegion) {
    if (securityRegion == null) {
      return null;
    }
    return switch (securityRegion) {
      case USA -> GeographicRegionType.US;
      case CANADA -> GeographicRegionType.CANADA;
      case EMERGING_MARKETS, OTHER -> GeographicRegionType.OTHER;
    };
  }

  private Map<GeographicRegionType, BigDecimal> fundAllocation(PortfolioHolding holding,
      HoldingGeographicAllocation allocation, List<Notification> warnings) {
    if (allocation == null) {
      warnings.add(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC.toNotificationForHolding(holding,
          getMetric().getUserFriendlyName()));
      return unknownAllocation();
    }
    if (allocation.getAllocations() == null || allocation.getAllocations().isEmpty()) {
      warnings.add(missingFundAllocationErrorCode().toNotificationForHolding(holding));
      return unknownAllocation();
    }
    return new EnumMap<>(allocation.getAllocations());
  }

  private Map<GeographicRegionType, BigDecimal> unknownAllocation() {
    Map<GeographicRegionType, BigDecimal> result = new EnumMap<>(GeographicRegionType.class);
    result.put(GeographicRegionType.UNKNOWN, BigDecimal.ONE);
    return result;
  }

  private Currency currencyFor(PortfolioHolding holding,
      Map<PortfolioHolding, Geography> stockGeographies,
      Map<PortfolioHolding, HoldingGeographicAllocation> fundExposures) {
    if (STOCK_PREDICATE.test(holding)) {
      return Optional.ofNullable(stockGeographies.get(holding))
          .map(Geography::getCurrency)
          .map(CurrencyDatapoint::getValue)
          .orElse(null);
    }
    return Optional.ofNullable(fundExposures.get(holding))
        .map(HoldingGeographicAllocation::getCurrency)
        .orElse(null);
  }

  private Map<GeographicRegionType, BigDecimal> aggregate(
      Map<PortfolioHolding, Map<GeographicRegionType, BigDecimal>> exposures,
      Map<PortfolioHolding, BigDecimal> weights) {
    Map<GeographicRegionType, BigDecimal> netProducts = new EnumMap<>(GeographicRegionType.class);
    for (GeographicRegionType region : GeographicRegionType.values()) {
      Map<PortfolioHolding, BigDecimal> regionValues = new HashMap<>();
      exposures.forEach((holding, perRegion) -> {
        if (perRegion != null && perRegion.containsKey(region)) {
          regionValues.put(holding, perRegion.get(region));
        }
      });
      netProducts.put(region, regionValues.isEmpty() ? BigDecimal.ZERO : sumProduct(regionValues, weights));
    }
    return netProducts;
  }

  protected Map<GeographicRegionType, BigDecimal> emptyRegionMap() {
    return Arrays.stream(GeographicRegionType.values())
        .collect(() -> new EnumMap<>(GeographicRegionType.class),
            (map, type) -> map.put(type, null),
            EnumMap::putAll);
  }
}
