package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
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
 * Emerging-markets-aware asset allocation breakdown service. Keeps the emerging-markets bucket separate from the
 * international-equities bucket; otherwise shares its allocation classification and currency-adjusted weighting with
 * {@link AssetAllocationServiceImpl}.
 */
@Service
public class AssetAllocationEMServiceImpl extends AbstractAssetAllocationService<AssetAllocationEMResult> {

  public AssetAllocationEMServiceImpl(SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher,
      SecurityDataFetcher<Geography> geographyFetcher, DefaultTargetCurrencyConverter currencyConverter,
      DefaultDataProperties defaultDataProperties) {
    super(assetAllocationFetcher, geographyFetcher, currencyConverter, defaultDataProperties);
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
