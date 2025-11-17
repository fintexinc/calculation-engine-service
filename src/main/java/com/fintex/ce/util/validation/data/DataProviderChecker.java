package com.fintex.ce.util.validation.data;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.calculation.AssetAllocationDataDTO;
import com.fintex.ce.model.redis.RAssetAllocation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.fintex.ce.util.validation.DataProviderRequestHandlingValidator.dataProviderCheckValidation;

@Component
public class DataProviderChecker {

    public void check(final List<DataProvider> dataProviders, final AssetAllocationDataDTO assetAllocationDataDto) {
        dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getEtfUsFdsResponse().values(), clearAssetAllocation());
        dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getEtfCanadaFdsResponse().values(), clearAssetAllocation());
        dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getMutualFundFdsResponse().values(), clearAssetAllocation());
        dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getBenchmarkIndexFdsResponse().values(), clearAssetAllocation());
        dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getCanadaPooledFundFdsResponse().values(), clearAssetAllocation());
        dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getCanadaHedgeFundsFdsResponse().values(), clearAssetAllocation());
        dataProviderCheckValidation(dataProviders, assetAllocationDataDto.getUsFundsFdsResponse().values(), clearAssetAllocation());
    }

    BiFunction<RAssetAllocation, Object, RAssetAllocation> clearAssetAllocation() {
        return (t, e) -> {
            t.setAssetAllocation(Map.of());
            return t;
        };
    }
}
