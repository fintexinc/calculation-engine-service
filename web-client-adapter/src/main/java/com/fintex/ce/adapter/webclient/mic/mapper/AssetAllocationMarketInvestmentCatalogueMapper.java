package com.fintex.ce.adapter.webclient.mic.mapper;

import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.AssetAllocation;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.AssetAllocationValue;
import com.fintex.wm.commons.domain.allocation.AssetAllocationWithCurrency;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Maps the MIC asset-allocation response ({@link AssetAllocationWithCurrency}) into the domain
 * {@link HoldingAssetAllocation}. Pulls the typed allocations from the nested {@link AssetAllocation}, the data
 * provider from the wrapper, and the trading currency from the {@link CurrencyDatapoint}.
 */
@Component
public class AssetAllocationMarketInvestmentCatalogueMapper
    implements
      MarketInvestmentCatalogueResponseMapper<HoldingAssetAllocation, AssetAllocationWithCurrency> {

  @Override
  public HoldingAssetAllocation map(AssetAllocationWithCurrency micResponse, PortfolioHolding holding) {
    return HoldingAssetAllocation.builder()
        .allocations(toTypedAllocations(micResponse))
        .currency(toCurrency(micResponse))
        .providers(providers(micResponse))
        .build();
  }

  private Map<AssetAllocationRegionType, BigDecimal> toTypedAllocations(AssetAllocationWithCurrency micResponse) {
    Map<AssetAllocationRegionType, BigDecimal> result = new EnumMap<>(AssetAllocationRegionType.class);
    if (micResponse == null || micResponse.getAssetAllocation() == null
        || CollectionUtils.isEmpty(micResponse.getAssetAllocation().getAllocations())) {
      return result;
    }
    for (AssetAllocationValue value : micResponse.getAssetAllocation().getAllocations()) {
      if (value == null || value.getType() == null || value.getValue() == null) {
        continue;
      }
      result.merge(value.getType(), value.getValue(), BigDecimal::add);
    }
    return result;
  }

  private Currency toCurrency(AssetAllocationWithCurrency micResponse) {
    return Optional.ofNullable(micResponse)
        .map(AssetAllocationWithCurrency::getCurrency)
        .map(CurrencyDatapoint::getValue)
        .orElse(null);
  }

  private List<DataProvider> providers(AssetAllocationWithCurrency micResponse) {
    if (micResponse == null) {
      return List.of();
    }
    List<DataProvider> providers = Optional.ofNullable(micResponse.getAssetAllocation())
        .map(AssetAllocation::getDataProviders)
        .orElseGet(micResponse::getDataProviders);
    return providers == null ? List.of() : providers;
  }
}
