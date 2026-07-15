package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationEMResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Asset allocation breakdown service for the {@code ASSET_ALLOCATIONS_EM} metric.
 * <p>
 * Historically this kept the emerging-markets equities bucket separate from international equities. Since the commons
 * model now consolidates all regional equities into a single {@link AssetAllocationRegionType#EQUITY} bucket, there is
 * no longer an emerging-markets bucket to separate, so this produces the same breakdown as
 * {@link AssetAllocationService}. It is retained for API/metric compatibility; removing the redundant
 * {@code ASSET_ALLOCATIONS_EM} metric is a product decision tracked separately (TMI-542 follow-up).
 */
@Service
public class AssetAllocationEmergingMarketsService extends AbstractAssetAllocationService<AssetAllocationEMResult> {

  public AssetAllocationEmergingMarketsService(SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher,
      SecurityDataFetcher<Geography> geographyFetcher, PortfolioWeightCalculator portfolioWeightCalculator,
      DefaultDataProperties defaultDataProperties) {
    super(assetAllocationFetcher, geographyFetcher, portfolioWeightCalculator, defaultDataProperties);
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
