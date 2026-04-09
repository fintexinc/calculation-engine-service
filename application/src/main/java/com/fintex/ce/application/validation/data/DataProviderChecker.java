package com.fintex.ce.application.validation.data;

import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.sm.model.DataProvider;

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

  public void check(final List<DataProvider> dataProviders, final AssetAllocationDataDTO assetAllocationDataDto) {
    final List<DataProvider> defaults = defaultDataProperties.getDataProviders();
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getEtfUsFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getEtfCanadaFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getMutualFundFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getBenchmarkIndexFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getCanadaPooledFundFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getCanadaHedgeFundsFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getUsFundsFdsResponse().values(),
        HoldingAssetAllocation::getProviders, defaults, clearAssetAllocation());
  }

  BiFunction<HoldingAssetAllocation, Object, HoldingAssetAllocation> clearAssetAllocation() {
    return (t, e) -> {
      t.setAllocations(Map.of());
      return t;
    };
  }
}
