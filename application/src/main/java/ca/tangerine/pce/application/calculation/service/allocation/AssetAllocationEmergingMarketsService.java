package ca.tangerine.pce.application.calculation.service.allocation;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import ca.tangerine.pce.application.calculation.service.PortfolioWeightCalculator;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.result.allocation.AssetAllocationEMResult;
import ca.tangerine.wm.commons.domain.allocation.AssetAllocationRegionType;
import ca.tangerine.wm.commons.error.Notification;

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
        .assetAllocationEmergingMarkets(netProducts)
        .warnings(warnings)
        .build();
  }
}
