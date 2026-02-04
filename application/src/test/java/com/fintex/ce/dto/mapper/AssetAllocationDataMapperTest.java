package com.fintex.ce.dto.mapper;

import com.fintex.ce.application.mapper.AssetAllocationDataMapper;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.AssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.util.ComparisonUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.util.MapUtils.overrideDefaultValues;
import static com.fintex.ce.util.TestConstants.GREATER_THAN_YEAR;
import static com.fintex.ce.util.TestConstants.LESS_THAN_YEAR;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.MapUtils.EMPTY_SORTED_MAP;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

class AssetAllocationDataMapperTest {

  @Test
  void mapForAAEM_checkResult() {
    // SETUP
    final var sut = mock(AssetAllocationDataMapper.class);

    final EtfHolding etfUs = mock(EtfHolding.class);
    final CanadaPooledFundHolding canadaPooledFundHolding = mock(CanadaPooledFundHolding.class);
    final CanadaHedgeFundHolding canadaHedgeFundHolding = mock(CanadaHedgeFundHolding.class);
    final EtfHolding etfCanada = mock(EtfHolding.class);
    final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);
    final UsMutualFundHolding usMutualFundHolding = mock(UsMutualFundHolding.class);
    final BenchmarkIndexHolding benchmarkIndexHolding = mock(BenchmarkIndexHolding.class);
    final FixedIncomeHolding fixedIncomeHolding = mock(FixedIncomeHolding.class);
    final SmaHolding smaHolding = mock(SmaHolding.class);
    final CashHolding cashHolding = new CashHolding(BigDecimal.valueOf(100_000_000), HoldingType.CASH);
    final Holding stocksHoldings = mock(Holding.class);
    final GicHolding gicHolding = new GicHolding(BigDecimal.valueOf(1), HoldingType.GIC);
    gicHolding.setTerm(LESS_THAN_YEAR);
    final Map<EtfHolding, AssetAllocation> etfCanadaAssetAllocation = new HashMap<>();
    final var rAssetAllocationForEtfCanada = new AssetAllocation().setHoldingType(HoldingType.CANADA_ETF)
        .setAssetAllocation(EMPTY_SORTED_MAP);
    etfCanadaAssetAllocation.put(etfCanada, rAssetAllocationForEtfCanada);

    final var req = new AssetAllocationDataDTO();
    req.setEtfUsFdsResponse(getFdsResponse(etfUs));
    req.setCanadaPooledFundFdsResponse(getFdsResponse(canadaPooledFundHolding));
    req.setCanadaHedgeFundsFdsResponse(getFdsResponse(canadaHedgeFundHolding));
    req.setUsFundsFdsResponse(getFdsResponse(usMutualFundHolding));
    req.setEtfCanadaFdsResponse(etfCanadaAssetAllocation);
    req.setMutualFundFdsResponse(getFdsResponse(fundSeriesHolding));
    req.setBenchmarkIndexFdsResponse(getFdsResponse(benchmarkIndexHolding));
    req.setFixedIncomeFdsResponse(getFdsResponse(fixedIncomeHolding));
    req.setSeparatelyManagedAccountFdsResponse(getFdsResponse(smaHolding));
    req.setHoldings(List.of(cashHolding, gicHolding));
    req.setStocksFdsResponse(getStocks(stocksHoldings));

    final Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> expected = getExpected(etfUs,
        fundSeriesHolding, benchmarkIndexHolding,
        canadaPooledFundHolding, canadaHedgeFundHolding, usMutualFundHolding, fixedIncomeHolding, smaHolding);
    expected.putAll(getExpectedWithSpecifiedDataProvider(null, stocksHoldings));
    expected.put(cashHolding, new ImmutablePair<>(null, overrideDefaultValues(AssetAllocationDataMapper.DEFAULT_MAP, Map
        .of(AssetAllocationRegion.CASH, BigDecimal.ONE))));
    expected.put(etfCanada, new ImmutablePair<>(null, overrideDefaultValues(AssetAllocationDataMapper.DEFAULT_MAP, Map
        .of(AssetAllocationRegion.UNCLASSIFIED, BigDecimal.ONE))));
    expected.put(gicHolding, new ImmutablePair<>(null, overrideDefaultValues(AssetAllocationDataMapper.DEFAULT_MAP, Map
        .of(AssetAllocationRegion.CASH, BigDecimal.ONE))));

    doCallRealMethod().when(sut).mapForAAEM(any());
    // ACT
    final Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> actual = sut.mapForAAEM(req);

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(expected, actual);
  }

  @Test
  void mapForAA_checkResult() {
    // SETUP
    final var sut = mock(AssetAllocationDataMapper.class);

    final EtfHolding etfUs = mock(EtfHolding.class);
    final CanadaPooledFundHolding canadaPooledFundHolding = mock(CanadaPooledFundHolding.class);
    final CanadaHedgeFundHolding canadaHedgeFundHolding = mock(CanadaHedgeFundHolding.class);
    final UsMutualFundHolding usMutualFundHolding = mock(UsMutualFundHolding.class);
    final FixedIncomeHolding fixedIncomeHolding = mock(FixedIncomeHolding.class);
    final SmaHolding smaHolding = mock(SmaHolding.class);
    final EtfHolding etfCanada = mock(EtfHolding.class);
    final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);
    final BenchmarkIndexHolding benchmarkIndexHolding = mock(BenchmarkIndexHolding.class);
    final CashHolding cashHolding = new CashHolding(BigDecimal.valueOf(100_000_000), HoldingType.CASH);
    final Holding stocksHoldings = mock(Holding.class);
    final GicHolding gicHolding = new GicHolding(BigDecimal.valueOf(1), HoldingType.GIC);
    gicHolding.setTerm(GREATER_THAN_YEAR);
    final Map<EtfHolding, AssetAllocation> etfCanadaAssetAllocation = new HashMap<>();
    final var rAssetAllocationForEtfCanada = new AssetAllocation().setHoldingType(HoldingType.CANADA_ETF)
        .setAssetAllocation(EMPTY_SORTED_MAP);
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

    final Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> expected = getExpected(etfUs,
        fundSeriesHolding,
        benchmarkIndexHolding, canadaPooledFundHolding, canadaHedgeFundHolding, usMutualFundHolding, fixedIncomeHolding,
        smaHolding);
    expected.putAll(getExpectedWithSpecifiedDataProvider(null, stocksHoldings));
    expected.put(cashHolding, new ImmutablePair<>(null, overrideDefaultValues(AssetAllocationDataMapper.DEFAULT_MAP, Map
        .of(AssetAllocationRegion.CASH, BigDecimal.ONE))));
    expected.put(etfCanada, new ImmutablePair<>(null, overrideDefaultValues(AssetAllocationDataMapper.DEFAULT_MAP, Map
        .of(AssetAllocationRegion.UNCLASSIFIED, BigDecimal.ONE))));
    expected.put(gicHolding, new ImmutablePair<>(null, overrideDefaultValues(AssetAllocationDataMapper.DEFAULT_MAP, Map
        .of(AssetAllocationRegion.FIXED_INCOME, BigDecimal.ONE))));
    final var expectedWithProperFormat = expected.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue()
        .getValue()));

    doCallRealMethod().when(sut).mapForAA(any());
    // ACT
    final var actual = sut.mapForAA(req);

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(expectedWithProperFormat, actual);
  }

  private HashMap<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> getExpected(
      final Holding... holdings) {
    return getExpectedWithSpecifiedDataProvider(DataProvider.EAGLE, holdings);
  }

  private HashMap<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> getExpectedWithSpecifiedDataProvider(
      final DataProvider provider, final Holding... holdings) {
    final var result = new HashMap<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>>();

    for (final Holding holding : holdings) {
      final Map<AssetAllocationRegion, BigDecimal> assetAllocation = new HashMap<>();
      for (final AssetAllocationRegion region : AssetAllocationRegion.values()) {
        assetAllocation.put(region, BigDecimal.valueOf(region.ordinal()));
      }

      result.put(holding, new ImmutablePair<>(provider, assetAllocation));
    }

    return result;
  }

  private <H extends Holding> Map<H, AssetAllocation> getFdsResponse(final H holding) {
    final var result = new HashMap<H, AssetAllocation>();

    final var assetAllocations = new HashMap<String, BigDecimal>();
    for (final var region : AssetAllocationRegion.values()) {
      assetAllocations.put(region.getName(), BigDecimal.valueOf(region.ordinal()));
    }

    final var rAssetAllocation = new AssetAllocation();
    rAssetAllocation.setHoldingType(HoldingType.US_ETF);
    rAssetAllocation.setAssetAllocation(assetAllocations);
    rAssetAllocation.setProvider(DataProvider.EAGLE.name());

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
