package com.fintex.ce.model.domain.calculation.allocation;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.Warning;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AssetAllocationData {

  Map<PortfolioHolding, HoldingAssetAllocation> etfUsFdsResponse;
  Map<PortfolioHolding, HoldingAssetAllocation> canadaPooledFundFdsResponse;
  Map<PortfolioHolding, HoldingAssetAllocation> canadaHedgeFundsFdsResponse;
  Map<PortfolioHolding, HoldingAssetAllocation> usFundsFdsResponse;
  Map<PortfolioHolding, HoldingAssetAllocation> etfCanadaFdsResponse;
  Map<PortfolioHolding, HoldingAssetAllocation> mutualFundFdsResponse;
  Map<PortfolioHolding, HoldingAssetAllocation> benchmarkIndexFdsResponse;
  Map<PortfolioHolding, HoldingAssetAllocation> fixedIncomeFdsResponse;
  Map<PortfolioHolding, HoldingAssetAllocation> separatelyManagedAccountFdsResponse;
  Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> stocksFdsResponse;

  List<? extends PortfolioHolding> holdings;

  List<Warning> warnings = new ArrayList<>();
}
