package com.fintex.ce.application.mapper.response;

import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegionType;
import com.fintex.ce.domain.model.AssetAllocation;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.application.result.AssetAllocationResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.port.mapper.ResponseMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.util.DecimalUtils.toUserScale;

@Component
public class AssetAllocationResponseMapper implements ResponseMapper<AssetAllocation, AssetAllocationResult> {

  @Override
  public AssetAllocationResult toResponse(AssetAllocation domain) {
    if (domain == null || domain.getAssetAllocation() == null) {
      return new AssetAllocationResult();
    }
    // Domain model uses String keys - convert to enum first
    Map<AssetAllocationRegion, BigDecimal> enumMap = convertToEnumMap(domain.getAssetAllocation());
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
  public AssetAllocationResult toResponse(Map<Holding, AssetAllocation> domainMap, List<Warning> warnings) {
    // This method would need aggregation logic - delegate to service for now
    throw new UnsupportedOperationException("Use service-level aggregation for AssetAllocation");
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
