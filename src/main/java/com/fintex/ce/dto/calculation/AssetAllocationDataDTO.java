package com.fintex.ce.dto.calculation;

import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.SmaHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RAssetAllocation;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class AssetAllocationDataDTO {

    Map<EtfHolding, RAssetAllocation> etfUsFdsResponse;
    Map<CanadaPooledFundHolding, RAssetAllocation> canadaPooledFundFdsResponse;
    Map<CanadaHedgeFundHolding, RAssetAllocation> canadaHedgeFundsFdsResponse;
    Map<UsMutualFundHolding, RAssetAllocation> usFundsFdsResponse;
    Map<EtfHolding, RAssetAllocation> etfCanadaFdsResponse;
    Map<FundSeriesHolding, RAssetAllocation> mutualFundFdsResponse;
    Map<BenchmarkIndexHolding, RAssetAllocation> benchmarkIndexFdsResponse;
    Map<FixedIncomeHolding, RAssetAllocation> fixedIncomeFdsResponse;
    Map<SmaHolding, RAssetAllocation> separatelyManagedAccountFdsResponse;
    Map<Holding, Map<AssetAllocationRegion, BigDecimal>> stocksFdsResponse;

    List<Holding> holdings;

    List<Warning> warnings = new ArrayList<>();
}