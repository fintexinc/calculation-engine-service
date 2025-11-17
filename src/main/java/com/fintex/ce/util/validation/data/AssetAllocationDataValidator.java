package com.fintex.ce.util.validation.data;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.dto.calculation.AssetAllocationDataDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RAssetAllocation;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class AssetAllocationDataValidator {

    public void validate(final AssetAllocationDataDTO assetAllocationDataDto, final List<Warning> warnings) {
        validateNonStock(assetAllocationDataDto.getEtfUsFdsResponse(), warnings);
        validateNonStock(assetAllocationDataDto.getEtfCanadaFdsResponse(), warnings);
        validateNonStock(assetAllocationDataDto.getMutualFundFdsResponse(), warnings);
        validateNonStock(assetAllocationDataDto.getBenchmarkIndexFdsResponse(), warnings);
        validateNonStock(assetAllocationDataDto.getCanadaPooledFundFdsResponse(), warnings);
        validateNonStock(assetAllocationDataDto.getCanadaHedgeFundsFdsResponse(), warnings);
        validateNonStock(assetAllocationDataDto.getUsFundsFdsResponse(), warnings);
        validateNonStock(assetAllocationDataDto.getFixedIncomeFdsResponse(), warnings);
        validateNonStock(assetAllocationDataDto.getSeparatelyManagedAccountFdsResponse(), warnings);
    }

    <H extends Holding> void validateNonStock(final Map<H, RAssetAllocation> holdings,
                                              final List<Warning> warnings) {
        holdings.forEach((holding, rAssetAllocation) -> validate(holding, rAssetAllocation.getAssetAllocation(), warnings));
    }

    void validate(final Holding holding,
                  final Map<String, BigDecimal> assetAllocations,
                  final List<Warning> warnings) {
        if (CollectionUtils.isEmpty(assetAllocations)) {
            validateWhenAssetAllocationIsEmpty(holding, warnings);
            return;
        }
        assetAllocations.keySet().forEach(region -> {
            final var assetAllocationRegion = AssetAllocationRegion.of(region);
            if (assetAllocationRegion == null || assetAllocationRegion.getName() == null) {
                warnings.add(ExceptionCode.WRN_UNKNOWN_001.warning(holding, region, "Asset Allocation"));
            }
        });
    }

    public void validateWhenAssetAllocationIsEmpty(final Holding holding, final List<Warning> warnings) {
        warnings.add(ExceptionCode.WRN_AA_AA_001.warning(holding));
    }

}
