package com.fintex.ce.application.mapping;

import com.fintex.ce.model.domain.calculation.AssetAllocationDataDTO;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.MapUtils.overrideDefaultValues;
import static com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion.UNCLASSIFIED;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

@Component
public class AssetAllocationDataMapper {

  public static final Map<AssetAllocationRegion, BigDecimal> DEFAULT_MAP;

  static {
    DEFAULT_MAP = unmodifiableMap(
        Stream.of(AssetAllocationRegion.values()).collect(toMap(e -> e, e -> BigDecimal.ZERO)));
  }

  public Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> mapForAA(AssetAllocationDataDTO dto) {
    final Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> result = new HashMap<>();
    result.putAll(mapForNoneStock(dto.getEtfUsFdsResponse()));
    result.putAll(mapForNoneStock(dto.getEtfCanadaFdsResponse()));
    result.putAll(mapForNoneStock(dto.getMutualFundFdsResponse()));
    result.putAll(mapForNoneStock(dto.getBenchmarkIndexFdsResponse()));
    result.putAll(mapForNoneStock(dto.getCanadaPooledFundFdsResponse()));
    result.putAll(mapForNoneStock(dto.getCanadaHedgeFundsFdsResponse()));
    result.putAll(mapForNoneStock(dto.getUsFundsFdsResponse()));
    result.putAll(mapForNoneStock(dto.getFixedIncomeFdsResponse()));
    result.putAll(mapForNoneStock(dto.getSeparatelyManagedAccountFdsResponse()));
    result.putAll(mapForCash(filterHoldings(dto.getHoldings(), CASH_PREDICATE)));
    result.putAll(mapForGic(filterHoldings(dto.getHoldings(), GIC_PREDICATE)));
    result.putAll(mapForStock(dto.getStocksFdsResponse()));
    return result;
  }

  private Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> mapForStock(
      final Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> stockFdsResponse) {
    return stockFdsResponse.entrySet().stream().collect(
        toMap(
            Map.Entry::getKey,
            e -> overrideDefaultValues(DEFAULT_MAP, e.getValue())));
  }

  private Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> mapForCash(
      final List<PortfolioHolding> holdings) {
    return holdings.stream().collect(Collectors.toMap(
        k -> k,
        v -> overrideDefaultValues(DEFAULT_MAP, Map.of(AssetAllocationRegion.CASH, BigDecimal.ONE))));
  }

  private Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> mapForGic(
      final List<PortfolioHolding> holdings) {
    final HashMap<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> result = new HashMap<>();
    for (final var holding : holdings) {
      final var gic = (GicHolding) holding;
      result.put(gic, overrideDefaultValues(DEFAULT_MAP, Map.of(gic.getAssetAllocation(), BigDecimal.ONE)));
    }
    return result;
  }

  private <H extends PortfolioHolding> Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> mapForNoneStock(
      final Map<H, HoldingAssetAllocation> holdings) {
    return holdings.entrySet().stream().collect(
        toMap(
            Map.Entry::getKey,
            e -> mapAllocationToRegions(e.getValue())));
  }

  /**
   * Maps asset allocations directly from REST API response to region exposures.
   *
   * @param allocations
   *          map of holdings to their asset allocation data
   * @return map of holdings to their region-based allocation breakdown
   */
  public Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> toRegionExposures(
      Map<PortfolioHolding, HoldingAssetAllocation> allocations) {
    if (CollectionUtils.isEmpty(allocations)) {
      return Collections.emptyMap();
    }
    return allocations.entrySet().stream()
        .collect(toMap(
            Map.Entry::getKey,
            e -> mapAllocationToRegions(e.getValue())));
  }

  private Map<AssetAllocationRegion, BigDecimal> mapAllocationToRegions(HoldingAssetAllocation allocation) {
    if (allocation == null || CollectionUtils.isEmpty(allocation.getAllocations())) {
      final var result = new EnumMap<>(DEFAULT_MAP);
      result.put(UNCLASSIFIED, BigDecimal.ONE);
      return result;
    }
    final Map<AssetAllocationRegion, BigDecimal> result = new EnumMap<>(AssetAllocationRegion.class);
    allocation.getAllocations().forEach((region, value) -> {
      final var assetAllocationRegion = AssetAllocationRegion.fromValue(region);
      if (assetAllocationRegion != null && assetAllocationRegion.getName() != null) {
        result.put(assetAllocationRegion, value);
      }
    });
    return overrideDefaultValues(DEFAULT_MAP, result);
  }
}
