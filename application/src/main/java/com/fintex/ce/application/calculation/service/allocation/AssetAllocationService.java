package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Asset allocation breakdown service. Aggregates asset-class exposures (cash, fixed income, the consolidated equity
 * bucket, etc.) using currency-adjusted portfolio weights.
 */
@Service
public class AssetAllocationService extends AbstractAssetAllocationService<AssetAllocationResult> {

  public AssetAllocationService(SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher,
      SecurityDataFetcher<Geography> geographyFetcher, PortfolioWeightCalculator portfolioWeightCalculator,
      DefaultDataProperties defaultDataProperties) {
    super(assetAllocationFetcher, geographyFetcher, portfolioWeightCalculator, defaultDataProperties);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ASSET_ALLOCATIONS;
  }

  @Override
  protected AssetAllocationResult buildResult(Map<AssetAllocationRegionType, BigDecimal> netProducts,
      List<Notification> warnings) {
    return AssetAllocationResult.builder()
        .assetAllocation(toUserScale(netProducts))
        .warnings(warnings)
        .build();
  }
}
