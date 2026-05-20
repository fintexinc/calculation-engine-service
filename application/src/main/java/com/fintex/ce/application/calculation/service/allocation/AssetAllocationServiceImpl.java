package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
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
 * Asset allocation breakdown service. Aggregates per-region exposures using currency-adjusted portfolio weights and
 * collapses the emerging-markets bucket into international equities for the regular AA view.
 */
@Service
public class AssetAllocationServiceImpl extends AbstractAssetAllocationService<AssetAllocationResult> {

  public AssetAllocationServiceImpl(SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher,
      SecurityDataFetcher<Geography> geographyFetcher, DefaultTargetCurrencyConverter currencyConverter,
      DefaultDataProperties defaultDataProperties) {
    super(assetAllocationFetcher, geographyFetcher, currencyConverter, defaultDataProperties);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ASSET_ALLOCATIONS;
  }

  @Override
  protected void postProcess(Map<AssetAllocationRegionType, BigDecimal> netProducts) {
    BigDecimal em = netProducts.remove(AssetAllocationRegionType.EM_EQUITIES);
    if (em == null || em.signum() == 0) {
      return;
    }
    netProducts.merge(AssetAllocationRegionType.INTERNATIONAL_EQUITIES, em, BigDecimal::add);
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
