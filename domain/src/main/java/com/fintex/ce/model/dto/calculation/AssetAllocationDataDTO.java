package com.fintex.ce.model.domain.calculation;

import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.error.Warning;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AssetAllocationDataDTO {

  Map<Holding, HoldingAssetAllocation> etfUsFdsResponse;
  Map<Holding, HoldingAssetAllocation> canadaPooledFundFdsResponse;
  Map<Holding, HoldingAssetAllocation> canadaHedgeFundsFdsResponse;
  Map<Holding, HoldingAssetAllocation> usFundsFdsResponse;
  Map<Holding, HoldingAssetAllocation> etfCanadaFdsResponse;
  Map<Holding, HoldingAssetAllocation> mutualFundFdsResponse;
  Map<Holding, HoldingAssetAllocation> benchmarkIndexFdsResponse;
  Map<Holding, HoldingAssetAllocation> fixedIncomeFdsResponse;
  Map<Holding, HoldingAssetAllocation> separatelyManagedAccountFdsResponse;
  Map<Holding, Map<AssetAllocationRegion, BigDecimal>> stocksFdsResponse;

  List<? extends Holding> holdings;

  List<Warning> warnings = new ArrayList<>();
}
