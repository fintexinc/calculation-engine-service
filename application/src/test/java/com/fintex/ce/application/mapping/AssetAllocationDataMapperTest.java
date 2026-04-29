package com.fintex.ce.application.mapping;

import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationData;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.MapUtils.overrideDefaultValues;
import static com.fintex.ce.application.util.TestConstants.GREATER_THAN_YEAR;
import static org.apache.commons.collections4.MapUtils.EMPTY_SORTED_MAP;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

class AssetAllocationDataMapperTest {

  @Test
  void shouldMapForAA_whenCheckResult() {
    // SETUP
    final var sut = mock(AssetAllocationDataMapper.class);

    final PortfolioHolding etfUs = mock(PortfolioHolding.class);
    final PortfolioHolding canadaPooledFundHolding = mock(PortfolioHolding.class);
    final PortfolioHolding canadaHedgeFundHolding = mock(PortfolioHolding.class);
    final PortfolioHolding usMutualFundHolding = mock(PortfolioHolding.class);
    final PortfolioHolding fixedIncomeHolding = mock(PortfolioHolding.class);
    final PortfolioHolding smaHolding = mock(PortfolioHolding.class);
    final PortfolioHolding etfCanada = mock(PortfolioHolding.class);
    final PortfolioHolding fundSeriesHolding = mock(PortfolioHolding.class);
    final PortfolioHolding benchmarkIndexHolding = mock(PortfolioHolding.class);
    final CashHolding cashHolding = CashHolding.builder().value(BigDecimal.valueOf(100_000_000)).holdingType(
        FinancialInstrumentType.CASH).build();
    final PortfolioHolding stocksHoldings = mock(PortfolioHolding.class);
    final GicHolding gicHolding = GicHolding.builder().value(BigDecimal.valueOf(1)).holdingType(
        FinancialInstrumentType.GIC)
        .term(GREATER_THAN_YEAR).build();
    final Map<PortfolioHolding, HoldingAssetAllocation> etfCanadaAssetAllocation = new HashMap<>();
    final var rAssetAllocationForEtfCanada = new HoldingAssetAllocation().setHoldingType(
        FinancialInstrumentType.ETF_CANADA)
        .setAllocations(EMPTY_SORTED_MAP);
    etfCanadaAssetAllocation.put(etfCanada, rAssetAllocationForEtfCanada);

    final var req = new AssetAllocationData();
    req.setEtfUsFdsResponse(getFdsResponse(etfUs));
    req.setCanadaPooledFundFdsResponse(getFdsResponse(canadaPooledFundHolding));
    req.setEtfCanadaFdsResponse(etfCanadaAssetAllocation);
    req.setMutualFundFdsResponse(getFdsResponse(fundSeriesHolding));
    req.setBenchmarkIndexFdsResponse(getFdsResponse(benchmarkIndexHolding));
    req.setHoldings(List.of(cashHolding, gicHolding));
    req.setStocksFdsResponse(getStocks(stocksHoldings));
    req.setCanadaHedgeFundsFdsResponse(getFdsResponse(canadaHedgeFundHolding));
    req.setUsFundsFdsResponse(getFdsResponse(usMutualFundHolding));
    req.setFixedIncomeFdsResponse(getFdsResponse(fixedIncomeHolding));
    req.setSeparatelyManagedAccountFdsResponse(getFdsResponse(smaHolding));

    final Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> expected = getExpected(etfUs,
        fundSeriesHolding,
        benchmarkIndexHolding, canadaPooledFundHolding, canadaHedgeFundHolding, usMutualFundHolding, fixedIncomeHolding,
        smaHolding);
    expected.putAll(getExpectedAllocations(stocksHoldings));
    expected.put(cashHolding, overrideDefaultValues(
        AssetAllocationDataMapper.DEFAULT_MAP, Map.of(AssetAllocationRegion.CASH, BigDecimal.ONE)));
    expected.put(etfCanada, overrideDefaultValues(AssetAllocationDataMapper.DEFAULT_MAP,
        Map.of(AssetAllocationRegion.UNCLASSIFIED, BigDecimal.ONE)));
    expected.put(gicHolding, overrideDefaultValues(AssetAllocationDataMapper.DEFAULT_MAP,
        Map.of(AssetAllocationRegion.FIXED_INCOME, BigDecimal.ONE)));

    doCallRealMethod().when(sut).mapForAA(any());
    // ACT
    final var actual = sut.mapForAA(req);

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(expected, actual);
  }

  private HashMap<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> getExpected(
      final PortfolioHolding... holdings) {
    return getExpectedAllocations(holdings);
  }

  private HashMap<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> getExpectedAllocations(
      final PortfolioHolding... holdings) {
    final var result = new HashMap<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>>();

    for (final PortfolioHolding holding : holdings) {
      final Map<AssetAllocationRegion, BigDecimal> assetAllocation = new HashMap<>();
      for (final AssetAllocationRegion region : AssetAllocationRegion.values()) {
        assetAllocation.put(region, BigDecimal.valueOf(region.ordinal()));
      }

      result.put(holding, assetAllocation);
    }

    return result;
  }

  private Map<PortfolioHolding, HoldingAssetAllocation> getFdsResponse(final PortfolioHolding holding) {
    final var result = new HashMap<PortfolioHolding, HoldingAssetAllocation>();

    final var assetAllocations = new HashMap<String, BigDecimal>();
    for (final var region : AssetAllocationRegion.values()) {
      assetAllocations.put(region.getName(), BigDecimal.valueOf(region.ordinal()));
    }

    final var rAssetAllocation = new HoldingAssetAllocation();
    rAssetAllocation.setHoldingType(FinancialInstrumentType.ETF_US);
    rAssetAllocation.setAllocations(assetAllocations);
    rAssetAllocation.setProviders(List.of(DataProvider.MORNINGSTAR));

    result.put(holding, rAssetAllocation);
    return result;
  }

  private Map<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>> getStocks(final PortfolioHolding holding) {
    final var result = new HashMap<PortfolioHolding, Map<AssetAllocationRegion, BigDecimal>>();

    final var assetAllocations = new HashMap<AssetAllocationRegion, BigDecimal>();
    for (final var region : AssetAllocationRegion.values()) {
      assetAllocations.put(region, BigDecimal.valueOf(region.ordinal()));
    }

    result.put(holding, assetAllocations);
    return result;
  }
}
