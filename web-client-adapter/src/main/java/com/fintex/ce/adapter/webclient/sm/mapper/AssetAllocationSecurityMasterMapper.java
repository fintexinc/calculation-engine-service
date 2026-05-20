package com.fintex.ce.adapter.webclient.sm.mapper;

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
 * Maps the SMS asset-allocation response ({@link AssetAllocationWithCurrency}) into the domain
 * {@link HoldingAssetAllocation}. Pulls the typed allocations from the nested {@link AssetAllocation}, the data
 * provider from the wrapper, and the trading currency from the {@link CurrencyDatapoint}.
 */
@Component
public class AssetAllocationSecurityMasterMapper
    implements
      SecurityMasterResponseMapper<HoldingAssetAllocation, AssetAllocationWithCurrency> {

  @Override
  public HoldingAssetAllocation map(AssetAllocationWithCurrency smsResponse, PortfolioHolding holding) {
    return HoldingAssetAllocation.builder()
        .allocations(toTypedAllocations(smsResponse))
        .currency(toCurrency(smsResponse))
        .providers(providers(smsResponse))
        .build();
  }

  private Map<AssetAllocationRegionType, BigDecimal> toTypedAllocations(AssetAllocationWithCurrency smsResponse) {
    Map<AssetAllocationRegionType, BigDecimal> result = new EnumMap<>(AssetAllocationRegionType.class);
    if (smsResponse == null || smsResponse.getAssetAllocation() == null
        || CollectionUtils.isEmpty(smsResponse.getAssetAllocation().getAllocations())) {
      return result;
    }
    for (AssetAllocationValue value : smsResponse.getAssetAllocation().getAllocations()) {
      if (value == null || value.getType() == null || value.getValue() == null) {
        continue;
      }
      result.merge(value.getType(), value.getValue(), BigDecimal::add);
    }
    return result;
  }

  private Currency toCurrency(AssetAllocationWithCurrency smsResponse) {
    return Optional.ofNullable(smsResponse)
        .map(AssetAllocationWithCurrency::getCurrency)
        .map(CurrencyDatapoint::getValue)
        .orElse(null);
  }

  private List<DataProvider> providers(AssetAllocationWithCurrency smsResponse) {
    if (smsResponse == null) {
      return List.of();
    }
    DataProvider provider = Optional.ofNullable(smsResponse.getAssetAllocation())
        .map(AssetAllocation::getDataProvider)
        .orElseGet(smsResponse::getDataProvider);
    return provider == null ? List.of() : List.of(provider);
  }
}
