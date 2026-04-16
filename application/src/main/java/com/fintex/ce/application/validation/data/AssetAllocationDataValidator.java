package com.fintex.ce.application.validation.data;

import com.fintex.ce.model.domain.calculation.AssetAllocationDataDTO;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.Warning;

import org.springframework.stereotype.Component;

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

  <H extends Holding> void validateNonStock(final Map<H, HoldingAssetAllocation> holdings,
      final List<Warning> warnings) {
    holdings.forEach((holding, assetAllocation) -> validate(holding, assetAllocation.getAllocations(), warnings));
  }

  void validate(final Holding holding,
      final Map<String, BigDecimal> assetAllocations,
      final List<Warning> warnings) {
    if (assetAllocations == null || assetAllocations.isEmpty()) {
      validateWhenAssetAllocationIsEmpty(holding, warnings);
      return;
    }
    assetAllocations.keySet().forEach(region -> {
      final var assetAllocationRegion = AssetAllocationRegion.fromValue(region);
      if (assetAllocationRegion == null || assetAllocationRegion.getName() == null) {
        warnings.add(ErrorCode.WRN_UNKNOWN_001.warning(holding, region, "Asset Allocation"));
      }
    });
  }

  public void validateWhenAssetAllocationIsEmpty(final Holding holding, final List<Warning> warnings) {
    warnings.add(ErrorCode.WRN_AA_AA_001.warning(holding));
  }

}
