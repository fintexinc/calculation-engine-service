package com.fintex.ce.application.mapping.response;

import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.AssetAllocationResult;
import com.fintex.ce.mapping.ResponseMapper;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import static com.fintex.ce.util.DecimalUtils.toUserScale;

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
    AssetAllocationResult assetAllocationResult = new AssetAllocationResult();
    assetAllocationResult.setAssetAllocation(toUserScale(result));
    assetAllocationResult.setWarnings(List.of());
    return assetAllocationResult;
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
  public AssetAllocationResult toResponse(Map<Holding, HoldingAssetAllocation> domainMap, List<Warning> warnings) {
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
      List<Warning> warnings) {
    Map<AssetAllocationRegionType, BigDecimal> result = calculateAssetAllocationResponse(netProducts);
    AssetAllocationResult assetAllocationResult = new AssetAllocationResult();
    assetAllocationResult.setAssetAllocation(toUserScale(result));
    assetAllocationResult.setWarnings(warnings);
    return assetAllocationResult;
  }
}
