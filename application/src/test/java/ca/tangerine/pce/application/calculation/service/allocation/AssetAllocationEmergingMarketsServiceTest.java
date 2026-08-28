package ca.tangerine.pce.application.calculation.service.allocation;

import ca.tangerine.pce.model.domain.result.allocation.AssetAllocationEMResult;
import ca.tangerine.wm.commons.domain.allocation.AssetAllocationRegionType;
import ca.tangerine.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.math.BigDecimal.ONE;

class AssetAllocationEmergingMarketsServiceTest extends AbstractAssetAllocationServiceTest<AssetAllocationEMResult> {

  @Override
  protected AbstractAssetAllocationService<AssetAllocationEMResult> createService() {
    return new AssetAllocationEmergingMarketsService(portfolioWeightCalculator);
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> getAllocation(AssetAllocationEMResult result) {
    return result.getAssetAllocationEmergingMarkets();
  }

  @Override
  protected List<Notification> getWarnings(AssetAllocationEMResult result) {
    return result.getWarnings();
  }

  @Override
  protected Set<AssetAllocationRegionType> emittedTypes() {
    return EnumSet.allOf(AssetAllocationRegionType.class);
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> expectedForEmergingMarketStockAlone() {
    return singleBucket(AssetAllocationRegionType.EM_EQUITIES, ONE);
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> expectedForCashHalfPlusEmStockHalf() {
    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.CASH, HALF);
    expected.put(AssetAllocationRegionType.EM_EQUITIES, HALF);
    return expected;
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> expectedForSamsungPlusEmEtf() {
    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.EM_EQUITIES, new BigDecimal("0.975"));
    expected.put(AssetAllocationRegionType.CASH, new BigDecimal("0.025"));
    return expected;
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> expectedForCalculateOnSingleEmEquity() {
    return singleBucket(AssetAllocationRegionType.EM_EQUITIES, ONE);
  }
}
