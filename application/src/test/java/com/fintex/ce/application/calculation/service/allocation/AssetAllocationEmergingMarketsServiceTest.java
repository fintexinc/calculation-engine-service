package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.model.domain.result.allocation.AssetAllocationEMResult;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.TestConstants.DEFAULT_DATA_PROPERTIES;

/**
 * The ASSET_ALLOCATIONS_EM metric now produces the same distribution as the regular AA metric (regional equities are
 * consolidated into a single EQUITY bucket in the commons model). The shared assertions in the parent verify that.
 */
class AssetAllocationEmergingMarketsServiceTest extends AbstractAssetAllocationServiceTest<AssetAllocationEMResult> {

  @Override
  protected AbstractAssetAllocationService<AssetAllocationEMResult> createService() {
    return new AssetAllocationEmergingMarketsService(assetAllocationFetcher, geographyFetcher,
        portfolioWeightCalculator, DEFAULT_DATA_PROPERTIES);
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> getAllocation(AssetAllocationEMResult result) {
    return result.getAssetAllocationEmergingMarkets();
  }

  @Override
  protected List<Notification> getWarnings(AssetAllocationEMResult result) {
    return result.getWarnings();
  }
}
