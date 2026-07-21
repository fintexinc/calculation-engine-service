package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationEMResult;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Emerging-markets-aware asset allocation breakdown service. Keeps the emerging-markets bucket separate from the
 * international-equities bucket; otherwise shares its allocation classification and currency-adjusted weighting with
 * {@link AssetAllocationService}.
 */
@Service
public class AssetAllocationEmergingMarketsService extends AbstractAssetAllocationService<AssetAllocationEMResult> {

  public AssetAllocationEmergingMarketsService(PortfolioWeightCalculator portfolioWeightCalculator) {
    super(portfolioWeightCalculator);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ASSET_ALLOCATIONS_EM;
  }

  @Override
  protected AssetAllocationEMResult buildResult(Map<AssetAllocationRegionType, BigDecimal> netProducts,
      List<Notification> warnings) {
    return AssetAllocationEMResult.builder()
        .assetAllocationEmergingMarkets(toUserScale(netProducts))
        .warnings(warnings)
        .build();
  }
}
