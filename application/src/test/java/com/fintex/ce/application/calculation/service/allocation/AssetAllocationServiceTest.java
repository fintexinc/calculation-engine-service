package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.model.domain.result.allocation.AssetAllocationResult;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.TestConstants.DEFAULT_DATA_PROPERTIES;

class AssetAllocationServiceTest extends AbstractAssetAllocationServiceTest<AssetAllocationResult> {

  @Override
  protected AbstractAssetAllocationService<AssetAllocationResult> createService() {
    return new AssetAllocationService(assetAllocationFetcher, geographyFetcher, portfolioWeightCalculator,
        DEFAULT_DATA_PROPERTIES);
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> getAllocation(AssetAllocationResult result) {
    return result.getAssetAllocation();
  }

  @Override
  protected List<Notification> getWarnings(AssetAllocationResult result) {
    return result.getWarnings();
  }
}
