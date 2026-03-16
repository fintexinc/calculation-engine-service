package com.fintex.ce.application.mapper;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.AssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.output.sm.dto.AssetAllocationDto;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import static com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion.UNCLASSIFIED;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static com.fintex.ce.util.MapUtils.overrideDefaultValues;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

@Component
public class AssetAllocationDataMapper {

  public static final Map<AssetAllocationRegion, BigDecimal> DEFAULT_MAP;

  static {
    DEFAULT_MAP = unmodifiableMap(
        Stream.of(AssetAllocationRegion.values()).collect(toMap(e -> e, e -> BigDecimal.ZERO)));
  }

  public Map<Holding, Map<AssetAllocationRegion, BigDecimal>> mapForAA(AssetAllocationDataDTO dto) {
    final Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> result = map(dto);
    return removeLeftPairElement(result);
  }

  public Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> mapForAAEM(
      AssetAllocationDataDTO dto) {
    return map(dto);
  }

  private Map<Holding, Map<AssetAllocationRegion, BigDecimal>> removeLeftPairElement(
      final Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> result) {
    return result.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().getValue()));
  }

  private Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> map(
      final AssetAllocationDataDTO dto) {
    final Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> result = new HashMap<>();
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

  private Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> mapForStock(
      final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> stockFdsResponse) {
    return stockFdsResponse.entrySet().stream().collect(
        toMap(
            Map.Entry::getKey,
            e -> Pair.of(null, overrideDefaultValues(DEFAULT_MAP, e.getValue()))));
  }

  private Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> mapForCash(
      final List<Holding> holdings) {
    return holdings.stream().collect(Collectors.toMap(
        k -> k,
        v -> Pair.of(null, overrideDefaultValues(DEFAULT_MAP, Map.of(AssetAllocationRegion.CASH, BigDecimal.ONE)))));
  }

  private Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> mapForGic(
      final List<Holding> holdings) {

    final HashMap<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> result = new HashMap<>();
    for (final var holding : holdings) {
      final var gic = (GicHolding) holding;
      result.put(gic, Pair.of(null, overrideDefaultValues(DEFAULT_MAP, Map.of(gic.getAssetAllocation(),
          BigDecimal.ONE))));
    }
    return result;
  }

  private <H extends Holding> Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> mapForNoneStock(
      final Map<H, AssetAllocation> holdings) {
    return holdings.entrySet().stream().collect(
        toMap(
            Map.Entry::getKey,
            e -> mapToRegions(Pair.of(DataProvider.of(e.getValue().getProvider()), e.getValue()
                .getAssetAllocation()))));
  }

  private Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> mapToRegions(
      final Pair<DataProvider, Map<String, BigDecimal>> pair) {
    if (CollectionUtils.isEmpty(pair.getValue())) {
      final var result = new EnumMap<>(DEFAULT_MAP);
      result.put(UNCLASSIFIED, BigDecimal.ONE);
      return Pair.of(pair.getKey(), result);
    }
    final Map<AssetAllocationRegion, BigDecimal> result = new EnumMap<>(AssetAllocationRegion.class);
    pair.getValue().forEach((region, value) -> {
      final var assetAllocationRegion = AssetAllocationRegion.of(region);
      if (assetAllocationRegion != null && assetAllocationRegion.getName() != null) {
        result.put(assetAllocationRegion, value);
      }
    });
    return Pair.of(pair.getKey(), overrideDefaultValues(DEFAULT_MAP, result));
  }

  /**
   * Maps asset allocations directly from REST API response to region exposures.
   *
   * @param allocations map of holdings to their asset allocation data
   * @return map of holdings to their region-based allocation breakdown
   */
  public Map<Holding, Map<AssetAllocationRegion, BigDecimal>> toRegionExposures(
      Map<Holding, AssetAllocationDto> allocations) {
    if (CollectionUtils.isEmpty(allocations)) {
      return Collections.emptyMap();
    }
    return allocations.entrySet().stream()
        .collect(toMap(
            Map.Entry::getKey,
            e -> mapAllocationToRegions(e.getValue())));
  }

  private Map<AssetAllocationRegion, BigDecimal> mapAllocationToRegions(AssetAllocationDto allocation) {
    if (allocation == null || CollectionUtils.isEmpty(allocation.getAssetAllocation())) {
      final var result = new EnumMap<>(DEFAULT_MAP);
      result.put(UNCLASSIFIED, BigDecimal.ONE);
      return result;
    }
    final Map<AssetAllocationRegion, BigDecimal> result = new EnumMap<>(AssetAllocationRegion.class);
    allocation.getAssetAllocation().forEach((region, value) -> {
      final var assetAllocationRegion = AssetAllocationRegion.of(region);
      if (assetAllocationRegion != null && assetAllocationRegion.getName() != null) {
        result.put(assetAllocationRegion, value);
      }
    });
    return overrideDefaultValues(DEFAULT_MAP, result);
  }
}
