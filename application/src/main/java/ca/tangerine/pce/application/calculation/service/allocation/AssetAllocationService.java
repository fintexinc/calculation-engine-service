package ca.tangerine.pce.application.calculation.service.allocation;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import ca.tangerine.pce.application.calculation.service.PortfolioWeightCalculator;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.result.allocation.AssetAllocationResult;
import ca.tangerine.wm.commons.domain.allocation.AssetAllocationRegionType;
import ca.tangerine.wm.commons.error.Notification;

/**
 * Asset allocation breakdown service. Aggregates per-region exposures using currency-adjusted portfolio weights and
 * collapses the emerging-markets bucket into international equities for the regular AA view.
 */
@Service
public class AssetAllocationService extends AbstractAssetAllocationService<AssetAllocationResult> {

  public AssetAllocationService(PortfolioWeightCalculator portfolioWeightCalculator) {
    super(portfolioWeightCalculator);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ASSET_ALLOCATIONS;
  }

  @Override
  protected Map<AssetAllocationRegionType, BigDecimal> collapseBuckets(
      Map<AssetAllocationRegionType, BigDecimal> netProducts) {
    BigDecimal em = netProducts.remove(AssetAllocationRegionType.EM_EQUITIES);
    if (em != null && em.signum() != 0) {
      netProducts.merge(AssetAllocationRegionType.INTERNATIONAL_EQUITIES, em, BigDecimal::add);
    }
    return netProducts;
  }

  @Override
  protected AssetAllocationResult buildResult(Map<AssetAllocationRegionType, BigDecimal> netProducts,
      List<Notification> warnings) {
    return AssetAllocationResult.builder()
        .assetAllocation(netProducts)
        .warnings(warnings)
        .build();
  }
}
