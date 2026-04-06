package com.fintex.ce.application.validation.data;

import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.sm.model.DataProvider;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.springframework.stereotype.Component;
import static com.fintex.ce.application.validation.DataProviderRequestHandlingValidator.dataProviderCheckValidation;

@Component
public class DataProviderChecker {

  public void check(final List<DataProvider> dataProviders, final AssetAllocationDataDTO assetAllocationDataDto) {
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getEtfUsFdsResponse().values(),
        clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getEtfCanadaFdsResponse().values(),
        clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getMutualFundFdsResponse().values(),
        clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getBenchmarkIndexFdsResponse().values(),
        clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getCanadaPooledFundFdsResponse().values(),
        clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getCanadaHedgeFundsFdsResponse().values(),
        clearAssetAllocation());
    dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getUsFundsFdsResponse().values(),
        clearAssetAllocation());
  }

  BiFunction<HoldingAssetAllocation, Object, HoldingAssetAllocation> clearAssetAllocation() {
    return (t, e) -> {
      t.setAllocations(Map.of());
      return t;
    };
  }
}
