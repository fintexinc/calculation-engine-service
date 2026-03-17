package com.fintex.ce.util.validation.data;

import com.fintex.ce.domain.enumeration.ExceptionCode;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.AssetAllocation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

  <H extends Holding> void validateNonStock(final Map<H, AssetAllocation> holdings,
      final List<Warning> warnings) {
    holdings.forEach((holding, assetAllocation) -> validate(holding, assetAllocation.getAssetAllocation(), warnings));
  }

  void validate(final Holding holding,
      final Map<String, BigDecimal> assetAllocations,
      final List<Warning> warnings) {
    if (assetAllocations == null || assetAllocations.isEmpty()) {
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
