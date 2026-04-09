package com.fintex.ce.application.mapping;

import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.TestConstants.GREATER_THAN_YEAR;
import static com.fintex.ce.util.MapUtils.overrideDefaultValues;
import static org.apache.commons.collections4.MapUtils.EMPTY_SORTED_MAP;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

class AssetAllocationDataMapperTest {

  @Test
  void shouldMapForAA_whenCheckResult() {
    // SETUP
    final var sut = mock(AssetAllocationDataMapper.class);

    final Holding etfUs = mock(Holding.class);
    final Holding canadaPooledFundHolding = mock(Holding.class);
    final Holding canadaHedgeFundHolding = mock(Holding.class);
    final Holding usMutualFundHolding = mock(Holding.class);
    final Holding fixedIncomeHolding = mock(Holding.class);
    final Holding smaHolding = mock(Holding.class);
    final Holding etfCanada = mock(Holding.class);
    final Holding fundSeriesHolding = mock(Holding.class);
    final Holding benchmarkIndexHolding = mock(Holding.class);
    final CashHolding cashHolding = CashHolding.builder().value(BigDecimal.valueOf(100_000_000)).holdingType(
        FinancialInstrumentType.CASH).build();
    final Holding stocksHoldings = mock(Holding.class);
    final GicHolding gicHolding = GicHolding.builder().value(BigDecimal.valueOf(1)).holdingType(
        FinancialInstrumentType.GIC)
        .term(GREATER_THAN_YEAR).build();
    final Map<Holding, HoldingAssetAllocation> etfCanadaAssetAllocation = new HashMap<>();
    final var rAssetAllocationForEtfCanada = new HoldingAssetAllocation().setHoldingType(
        FinancialInstrumentType.ETF_CANADA)
        .setAllocations(EMPTY_SORTED_MAP);
    etfCanadaAssetAllocation.put(etfCanada, rAssetAllocationForEtfCanada);

    final var req = new AssetAllocationDataDTO();
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

    final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> expected = getExpected(etfUs,
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

  private HashMap<Holding, Map<AssetAllocationRegion, BigDecimal>> getExpected(final Holding... holdings) {
    return getExpectedAllocations(holdings);
  }

  private HashMap<Holding, Map<AssetAllocationRegion, BigDecimal>> getExpectedAllocations(final Holding... holdings) {
    final var result = new HashMap<Holding, Map<AssetAllocationRegion, BigDecimal>>();

    for (final Holding holding : holdings) {
      final Map<AssetAllocationRegion, BigDecimal> assetAllocation = new HashMap<>();
      for (final AssetAllocationRegion region : AssetAllocationRegion.values()) {
        assetAllocation.put(region, BigDecimal.valueOf(region.ordinal()));
      }

      result.put(holding, assetAllocation);
    }

    return result;
  }

  private Map<Holding, HoldingAssetAllocation> getFdsResponse(final Holding holding) {
    final var result = new HashMap<Holding, HoldingAssetAllocation>();

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

  private Map<Holding, Map<AssetAllocationRegion, BigDecimal>> getStocks(final Holding holding) {
    final var result = new HashMap<Holding, Map<AssetAllocationRegion, BigDecimal>>();

    final var assetAllocations = new HashMap<AssetAllocationRegion, BigDecimal>();
    for (final var region : AssetAllocationRegion.values()) {
      assetAllocations.put(region, BigDecimal.valueOf(region.ordinal()));
    }

    result.put(holding, assetAllocations);
    return result;
  }
}
