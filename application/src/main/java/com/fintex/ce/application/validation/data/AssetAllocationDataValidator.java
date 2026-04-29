package com.fintex.ce.application.validation.data;

import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationData;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.Warning;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class AssetAllocationDataValidator {

  public void validate(final AssetAllocationData assetAllocationData, final List<Warning> warnings) {
    validateNonStock(assetAllocationData.getEtfUsFdsResponse(), warnings);
    validateNonStock(assetAllocationData.getEtfCanadaFdsResponse(), warnings);
    validateNonStock(assetAllocationData.getMutualFundFdsResponse(), warnings);
    validateNonStock(assetAllocationData.getBenchmarkIndexFdsResponse(), warnings);
    validateNonStock(assetAllocationData.getCanadaPooledFundFdsResponse(), warnings);
    validateNonStock(assetAllocationData.getCanadaHedgeFundsFdsResponse(), warnings);
    validateNonStock(assetAllocationData.getUsFundsFdsResponse(), warnings);
    validateNonStock(assetAllocationData.getFixedIncomeFdsResponse(), warnings);
    validateNonStock(assetAllocationData.getSeparatelyManagedAccountFdsResponse(), warnings);
  }

  <H extends PortfolioHolding> void validateNonStock(final Map<H, HoldingAssetAllocation> holdings,
      final List<Warning> warnings) {
    holdings.forEach((holding, assetAllocation) -> validate(holding, assetAllocation.getAllocations(), warnings));
  }

  void validate(final PortfolioHolding holding,
      final Map<String, BigDecimal> assetAllocations,
      final List<Warning> warnings) {
    if (assetAllocations == null || assetAllocations.isEmpty()) {
      validateWhenAssetAllocationIsEmpty(holding, warnings);
      return;
    }
    assetAllocations.keySet().forEach(region -> {
      final var assetAllocationRegion = AssetAllocationRegion.fromValue(region);
      if (assetAllocationRegion == null || assetAllocationRegion.getName() == null) {
        warnings.add(ErrorCode.UNKNOWN_TYPE_FROM_DATA_POINT.warning(holding, region, "Asset Allocation"));
      }
    });
  }

  public void validateWhenAssetAllocationIsEmpty(final PortfolioHolding holding, final List<Warning> warnings) {
    warnings.add(ErrorCode.MISSING_ASSET_ALLOCATION.warning(holding));
  }

}
