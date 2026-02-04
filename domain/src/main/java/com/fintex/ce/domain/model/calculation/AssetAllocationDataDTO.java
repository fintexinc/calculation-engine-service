package com.fintex.ce.domain.model.calculation;

import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.AssetAllocation;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class AssetAllocationDataDTO {

  Map<EtfHolding, AssetAllocation> etfUsFdsResponse;
  Map<CanadaPooledFundHolding, AssetAllocation> canadaPooledFundFdsResponse;
  Map<CanadaHedgeFundHolding, AssetAllocation> canadaHedgeFundsFdsResponse;
  Map<UsMutualFundHolding, AssetAllocation> usFundsFdsResponse;
  Map<EtfHolding, AssetAllocation> etfCanadaFdsResponse;
  Map<FundSeriesHolding, AssetAllocation> mutualFundFdsResponse;
  Map<BenchmarkIndexHolding, AssetAllocation> benchmarkIndexFdsResponse;
  Map<FixedIncomeHolding, AssetAllocation> fixedIncomeFdsResponse;
  Map<SmaHolding, AssetAllocation> separatelyManagedAccountFdsResponse;
  Map<Holding, Map<AssetAllocationRegion, BigDecimal>> stocksFdsResponse;

  List<Holding> holdings;

  List<Warning> warnings = new ArrayList<>();
}
