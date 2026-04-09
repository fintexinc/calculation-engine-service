package com.fintex.ce.domain.model.calculation;

import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;

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
