package com.fintex.ce.application.validation.data;

import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationData;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.wm.commons.domain.DataProvider;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.application.validation.DataProviderRequestHandlingValidator.dataProviderCheckValidation;

@Component
@RequiredArgsConstructor
public class DataProviderChecker {

  private final DefaultDataProperties defaultDataProperties;

  public void check(final List<DataProvider> dataProviders, final AssetAllocationData assetAllocationData) {
    final List<DataProvider> defaults = defaultDataProperties.getDataProviders();
    dataProviderCheckValidation(dataProviders, assetAllocationData.getEtfUsFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationData.getEtfCanadaFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationData.getMutualFundFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationData.getBenchmarkIndexFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationData.getCanadaPooledFundFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationData.getCanadaHedgeFundsFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationData.getUsFundsFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
  }

  BiFunction<HoldingAssetAllocation, Object, HoldingAssetAllocation> clearAssetAllocation() {
    return (t, e) -> {
      t.setAllocations(Map.of());
      return t;
    };
  }
}
