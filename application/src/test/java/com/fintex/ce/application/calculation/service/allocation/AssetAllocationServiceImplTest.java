package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.model.domain.result.allocation.AssetAllocationResult;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.application.util.TestConstants.DEFAULT_DATA_PROPERTIES;
import static java.math.BigDecimal.ONE;

class AssetAllocationServiceImplTest extends AbstractAssetAllocationServiceTest<AssetAllocationResult> {

  @Override
  protected AbstractAssetAllocationService<AssetAllocationResult> createService() {
    return new AssetAllocationServiceImpl(assetAllocationFetcher, geographyFetcher, currencyConverter,
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

  @Override
  protected Set<AssetAllocationRegionType> emittedTypes() {
    return EnumSet.complementOf(EnumSet.of(AssetAllocationRegionType.EM_EQUITIES));
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> expectedForEmergingMarketStockAlone() {
    return singleBucket(AssetAllocationRegionType.INTERNATIONAL_EQUITIES, ONE);
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> expectedForCashHalfPlusEmStockHalf() {
    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.CASH, HALF);
    expected.put(AssetAllocationRegionType.INTERNATIONAL_EQUITIES, HALF);
    return expected;
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> expectedForSamsungPlusEmEtf() {
    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.INTERNATIONAL_EQUITIES, new BigDecimal("0.975"));
    expected.put(AssetAllocationRegionType.CASH, new BigDecimal("0.025"));
    return expected;
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> expectedForCalculateOnSingleEmEquity() {
    return singleBucket(AssetAllocationRegionType.INTERNATIONAL_EQUITIES, ONE);
  }
}
