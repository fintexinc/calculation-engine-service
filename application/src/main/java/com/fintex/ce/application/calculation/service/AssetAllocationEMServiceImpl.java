package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.application.mapping.AssetAllocationDataMapper;
import com.fintex.ce.application.mapping.CountryAllocationMappingService;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegionEmType;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationEMResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.application.util.CalculationUtils.sum;
import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.ASIA_PACIFIC_EQUITIES;
import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.CANADIAN_EQUITIES;
import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.CASH;
import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.EM_EQUITIES;
import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.EUROPEAN_EQUITIES;
import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.FIXED_INCOME;
import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.INTERNATIONAL_EQUITIES;
import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.OTHER;
import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.UNCLASSIFIED;
import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.US_EQUITIES;
import static com.fintex.ce.model.domain.calculation.allocation.CountryRegionType.UNITED_STATES;
import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_COUNTRY_EXPOSURE;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static java.math.BigDecimal.ZERO;

@Service
@RequiredArgsConstructor
public class AssetAllocationEMServiceImpl
    extends
      BreakdownAbstractService<AssetAllocationEMResult, AssetAllocationRegionEmType> {

  private final SecurityDataFetcher<EquityCountryAllocation> countryAllocationSecurityDataFetcher;
  private final SecurityDataFetcher<HoldingAssetAllocation> assetAllocationSecurityDataFetcher;
  private final AssetAllocationDataMapper assetAllocationDataMapper;
  private final CountryAllocationMappingService countryAllocationMappingService;
  private final DefaultDataProperties defaultDataProperties;

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ASSET_ALLOCATIONS_EM;
  }

  @Override
  public AssetAllocationEMResult calculate(ExposureDataHolder<AssetAllocationRegionEmType> exposureData,
      List<PortfolioHolding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    final Map<AssetAllocationRegionEmType, BigDecimal> result = calculateNetProducts(exposures, holdings,
        AssetAllocationRegionEmType.values());
    return AssetAllocationEMResult.builder()
        .assetAllocationEmergingMarkets(DecimalUtils.toUserScale(result))
        .warnings(warnings)
        .build();
  }

  @Override
  public ExposureDataHolder<AssetAllocationRegionEmType> fetchExposures(final PortfolioHoldingsCommand command) {
    final Map<PortfolioHolding, HoldingAssetAllocation> rawData = assetAllocationSecurityDataFetcher.fetch(
        command.getHoldings(),
        getSpecifiedIfEmpty(command.getDataProviders(), defaultDataProperties.getDataProviders()));
    final var assetAllocations = assetAllocationDataMapper.toRegionExposures(rawData);

    return calculateAssetAllocationEMarketMap(
        command.getHoldings(),
        assetAllocations,
        getSpecifiedIfEmpty(command.getDataProviders(), defaultDataProperties.getDataProviders()));
  }

  public ExposureDataHolder<AssetAllocationRegionEmType> calculateAssetAllocationEMarketMap(
      final List<PortfolioHolding> holdings,
      final Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> assetAllocations,
      final List<DataProvider> providers) {
    List<Notification> warnings = new ArrayList<>();
    Map<PortfolioHolding, EquityCountryAllocation> rawCountryAllocations = countryAllocationSecurityDataFetcher.fetch(
        holdings, providers);
    Map<PortfolioHolding, Map<String, BigDecimal>> holdingAllocations = rawCountryAllocations.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    final Map<PortfolioHolding, Map<CountryRegionType, BigDecimal>> countryAllocationsMap = countryAllocationMappingService
        .mapToCountryRegions(holdingAllocations, warnings, MISSING_EQUITY_COUNTRY_EXPOSURE);
    final Map<PortfolioHolding, BigDecimal> equityDifference = calculateEquityDifference(
        holdings, countryAllocationsMap, assetAllocations);
    Map<PortfolioHolding, Map<AssetAllocationRegionEmType, BigDecimal>> allocations = holdings.stream().collect(
        toMap(
            h -> h,
            h -> calculateEmergingMarket(assetAllocations, countryAllocationsMap, equityDifference, h)));
    return new ExposureDataHolder<>(allocations, warnings);
  }

  public Map<AssetAllocationRegionEmType, BigDecimal> calculateEmergingMarket(
      final Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> assetAllocations,
      final Map<PortfolioHolding, Map<CountryRegionType, BigDecimal>> countryAllocationsMap,
      final Map<PortfolioHolding, BigDecimal> equityDifference,
      final PortfolioHolding holding) {
    final Map<CountryRegionType, BigDecimal> countryAllocations = countryAllocationsMap.get(holding);
    final Map<AssetAllocationRegion, BigDecimal> holdingAssetAllocations = assetAllocations.get(holding);
    return Stream.of(AssetAllocationRegionEmType.values())
        .collect(toMap(t -> t, t -> getEmergingMarketValue(holding, holdingAssetAllocations, countryAllocations,
            equityDifference, t)));
  }

  public BigDecimal getEmergingMarketValue(final PortfolioHolding holding,
      final Map<AssetAllocationRegion, BigDecimal> assetAllocations,
      final Map<CountryRegionType, BigDecimal> countryAllocations,
      final Map<PortfolioHolding, BigDecimal> equityDifference,
      final AssetAllocationRegionEmType type) {
    if (assetAllocations.isEmpty()) {
      return ZERO;
    }
    if (AssetAllocationRegionEmType.CASH.equals(type)) {
      return assetAllocations.get(CASH);
    } else if (AssetAllocationRegionEmType.FIXED_INCOME.equals(type)) {
      return assetAllocations.get(FIXED_INCOME);
    } else if (AssetAllocationRegionEmType.CANADIAN_EQUITY.equals(type)) {
      return Objects.requireNonNull(getCountryValue(countryAllocations, CountryRegionType.CANADA));
    } else if (AssetAllocationRegionEmType.US_EQUITY.equals(type)) {
      return Objects.requireNonNull(getCountryValue(countryAllocations, UNITED_STATES));
    } else if (AssetAllocationRegionEmType.INTERNATIONAL_EQUITY.equals(type)) {
      return emForInternationalEquity(holding, countryAllocations, equityDifference);
    } else if (AssetAllocationRegionEmType.EMERGING_MARKET_EQUITY.equals(type)) {
      return Objects.requireNonNull(Optional.ofNullable(countryAllocations)
          .map(p -> p.get(CountryRegionType.EMERGING_MARKET)).orElse(ZERO));
    } else if (AssetAllocationRegionEmType.UNCLASSIFIED.equals(type)) {
      return Objects.requireNonNull(assetAllocations.get(UNCLASSIFIED));
    } else {
      return Objects.requireNonNull(assetAllocations.get(OTHER));
    }
  }

  private BigDecimal getCountryValue(final Map<CountryRegionType, BigDecimal> countryAllocations,
      final CountryRegionType type) {
    return Optional.ofNullable(countryAllocations).map(p -> p.get(type)).orElse(ZERO);
  }

  public BigDecimal emForInternationalEquity(final PortfolioHolding holding,
      final Map<CountryRegionType, BigDecimal> countryAllocations,
      final Map<PortfolioHolding, BigDecimal> equityDifference) {
    final BigDecimal equityDiff = equityDifference.get(holding);
    final BigDecimal internationalValue = Optional.ofNullable(countryAllocations)
        .map(p -> p.get(CountryRegionType.INTERNATIONAL_DEVELOPED)).orElse(ZERO);
    return internationalValue.add(equityDiff);
  }

  public Map<PortfolioHolding, BigDecimal> calculateEquityDifference(final List<PortfolioHolding> holdings,
      final Map<PortfolioHolding, Map<CountryRegionType, BigDecimal>> countryAllocations,
      final Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> assetAllocations) {
    final Set<AssetAllocationRegion> equities = Set.of(CANADIAN_EQUITIES, US_EQUITIES, EUROPEAN_EQUITIES,
        ASIA_PACIFIC_EQUITIES, EM_EQUITIES, INTERNATIONAL_EQUITIES);
    return holdings.stream().collect(toMap(h -> h, h -> calculateEquityDiff(countryAllocations, assetAllocations,
        equities, h)));
  }

  public BigDecimal calculateEquityDiff(
      final Map<PortfolioHolding, Map<CountryRegionType, BigDecimal>> countryAllocations,
      final Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> assetAllocations,
      final Set<AssetAllocationRegion> equities,
      final PortfolioHolding holding) {
    final BigDecimal equityCountrySum = countryAllocations.containsKey(holding)
        ? sum(countryAllocations.get(holding))
        : ZERO;
    final BigDecimal assetAllocationEquitySum = Objects.requireNonNull(assetAllocations.get(holding)).entrySet()
        .stream()
        .filter(e -> equities.contains(e.getKey())).map(Map.Entry::getValue).reduce(ZERO, BigDecimal::add);
    return assetAllocationEquitySum.subtract(equityCountrySum);
  }

}
