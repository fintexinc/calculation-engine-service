package com.fintex.ce.application.mapping.response;

import com.fintex.ce.mapping.ResponseMapper;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegionType;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationResult;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;

@Component
public class AssetAllocationResponseMapper implements ResponseMapper<HoldingAssetAllocation, AssetAllocationResult> {

  @Override
  public AssetAllocationResult toResponse(HoldingAssetAllocation domain) {
    if (domain == null || domain.getAllocations() == null) {
      return new AssetAllocationResult();
    }
    // Domain model uses String keys - convert to enum first
    Map<AssetAllocationRegion, BigDecimal> enumMap = convertToEnumMap(domain.getAllocations());
    Map<AssetAllocationRegionType, BigDecimal> result = calculateAssetAllocationResponse(enumMap);
    return AssetAllocationResult.builder()
        .assetAllocation(toUserScale(result))
        .warnings(List.of())
        .build();
  }

  /**
   * Converts String-keyed map from domain model to enum-keyed map.
   */
  private Map<AssetAllocationRegion, BigDecimal> convertToEnumMap(Map<String, BigDecimal> stringMap) {
    Map<AssetAllocationRegion, BigDecimal> result = new EnumMap<>(AssetAllocationRegion.class);
    for (Map.Entry<String, BigDecimal> entry : stringMap.entrySet()) {
      try {
        AssetAllocationRegion region = AssetAllocationRegion.valueOf(entry.getKey());
        result.put(region, entry.getValue());
      } catch (IllegalArgumentException ignored) {
        // Skip unknown region keys
      }
    }
    return result;
  }

  @Override
  public AssetAllocationResult toResponse(Map<PortfolioHolding, HoldingAssetAllocation> domainMap,
      List<Notification> warnings) {
    // This method would need aggregation logic - delegate to service for now
    throw new UnsupportedOperationException("Use service-level aggregation for HoldingAssetAllocation");
  }

  /**
   * Converts internal AssetAllocationRegion values to response AssetAllocationRegionType.
   */
  public Map<AssetAllocationRegionType, BigDecimal> calculateAssetAllocationResponse(
      Map<AssetAllocationRegion, BigDecimal> allocationPerType) {
    Map<AssetAllocationRegionType, BigDecimal> result = new EnumMap<>(AssetAllocationRegionType.class);
    for (Map.Entry<AssetAllocationRegion, BigDecimal> entry : allocationPerType.entrySet()) {
      result.putIfAbsent(entry.getKey().getAssetAllocationRegionType(), BigDecimal.ZERO);
      result.computeIfPresent(entry.getKey().getAssetAllocationRegionType(),
          (type, sum) -> sum.add(entry.getValue()));
    }
    return result;
  }

  /**
   * Creates response from pre-calculated net products.
   */
  public AssetAllocationResult fromNetProducts(Map<AssetAllocationRegion, BigDecimal> netProducts,
      List<Notification> warnings) {
    Map<AssetAllocationRegionType, BigDecimal> result = calculateAssetAllocationResponse(netProducts);
    return AssetAllocationResult.builder()
        .assetAllocation(toUserScale(result))
        .warnings(warnings)
        .build();
  }
}
