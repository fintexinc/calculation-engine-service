package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.core.CacheStorageAbstract;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import com.fintex.ce.domain.enumeration.Country;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.AssetAllocation;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
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
import com.fintex.ce.adapter.cache.entity.RAssetAllocation;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.cache.AssetAllocationCachePort;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.adapter.cache.repository.assetallocation.AssetAllocationRepository;
import com.fintex.ce.util.CollectorUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.SEPARATELY_MANAGED_ACCOUNT_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;

@Service
public class AssetAllocationCacheStorage
    extends CacheStorageAbstract<AssetAllocation, RAssetAllocation, AssetAllocationDataDTO> implements AssetAllocationCachePort {

  private final BusinessCountryCacheStorage businessCountryCacheStorage;

  public AssetAllocationCacheStorage(
      final SecurityDataPort<AssetAllocation> securityDataPort,
      final CacheEntityMapper<AssetAllocation, RAssetAllocation> mapper,
      final AssetAllocationRepository assetAllocationRepository,
      final BusinessCountryCacheStorage businessCountryCacheStorage,
      final CacheStatisticService cacheStatisticService) {
    super(securityDataPort, mapper, assetAllocationRepository, cacheStatisticService, CacheNameEntity.ASSET_ALLOCATION);
    this.businessCountryCacheStorage = businessCountryCacheStorage;
  }

  @Override
  public AssetAllocationDataDTO load(final List<? extends Holding> holdings, final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    return load(holdings, providers, false, warnings);
  }

  public AssetAllocationDataDTO loadWithDataProvidersCheck(final List<? extends Holding> holdings,
                                                           final List<DataProvider> dataProviders,
                                                           final List<Warning> warnings) {
    return load(holdings, dataProviders, true, warnings);
  }

  public AssetAllocationDataDTO load(final List<? extends Holding> holdings,
      final List<DataProvider> dataProviders,
      final boolean needToCheckDataProvidersFromResponse,
      final List<Warning> warnings) {
    final Map<EtfHolding, com.fintex.ce.domain.model.AssetAllocation> etfUsFdsResponse = loadForBenchOfEtfUs(
        filterHoldings(holdings, US_ETF_PREDICATE), List.of());
    final Map<EtfHolding, com.fintex.ce.domain.model.AssetAllocation> etfCanadaFdsResponse = loadForBenchOfEtfCanada(
        filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of());
    final Map<FundSeriesHolding, com.fintex.ce.domain.model.AssetAllocation> mutualFundFdsResponse = loadBenchOfFundCanada(
        filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), dataProviders);
    final Map<BenchmarkIndexHolding, com.fintex.ce.domain.model.AssetAllocation> benchmarkIndexFdsResponse = loadForBenchOfBenchmarks(
        filterHoldings(holdings, BENCHMARKS_PREDICATE), dataProviders);
    final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> stocksFdsResponse = loadStocks(holdings, dataProviders,
        needToCheckDataProvidersFromResponse, warnings);
    final Map<CanadaPooledFundHolding, com.fintex.ce.domain.model.AssetAllocation> pooledFunds = loadCanadaPooledFunds(
        filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), dataProviders);
    final Map<CanadaHedgeFundHolding, com.fintex.ce.domain.model.AssetAllocation> hedgeFunds = loadCanadaHedgeFunds(
        filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), dataProviders);
    final Map<UsMutualFundHolding, com.fintex.ce.domain.model.AssetAllocation> usMutualFunds = loadUsMutualFunds(
        filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), dataProviders);
    final Map<FixedIncomeHolding, com.fintex.ce.domain.model.AssetAllocation> fixedIncomes = loadBenchOfFixedIncomes(
        filterHoldings(holdings, FIXED_INCOME_PREDICATE), dataProviders);
    final Map<SmaHolding, com.fintex.ce.domain.model.AssetAllocation> separatelyManagedAccounts = loadBenchOfSeparatelyManagedAccounts(
        filterHoldings(holdings, SEPARATELY_MANAGED_ACCOUNT_PREDICATE), dataProviders);

    return new AssetAllocationDataDTO()
        .setEtfUsFdsResponse(etfUsFdsResponse)
        .setEtfCanadaFdsResponse(etfCanadaFdsResponse)
        .setMutualFundFdsResponse(mutualFundFdsResponse)
        .setBenchmarkIndexFdsResponse(benchmarkIndexFdsResponse)
        .setHoldings(holdings)
        .setStocksFdsResponse(stocksFdsResponse)
        .setCanadaPooledFundFdsResponse(pooledFunds)
        .setCanadaHedgeFundsFdsResponse(hedgeFunds)
        .setUsFundsFdsResponse(usMutualFunds)
        .setFixedIncomeFdsResponse(fixedIncomes)
        .setSeparatelyManagedAccountFdsResponse(separatelyManagedAccounts);
  }

  public Map<Holding, Map<AssetAllocationRegion, BigDecimal>> loadStocks(final List<? extends Holding> holdings,
      final List<DataProvider> dataProviders,
      final boolean needToCheckDataProvidersFromResponse,
      final List<Warning> warnings) {
    final Map<Holding, Country> countries = businessCountryCacheStorage.loadBusinessCountries(
        filterHoldings(holdings, STOCK_PREDICATE),
        dataProviders,
        needToCheckDataProvidersFromResponse,
        warnings);

    return countries.entrySet().stream()
        .filter(c -> Objects.nonNull(c.getValue()))
        .collect(
            CollectorUtils.toMap(
                Map.Entry::getKey,
                e -> Map.of(defineType(e.getValue()), BigDecimal.ONE)));
  }

  private AssetAllocationRegion defineType(final Country country) {
    return switch (country) {
      case CAN -> AssetAllocationRegion.CANADIAN_EQUITIES;
      case USA -> AssetAllocationRegion.US_EQUITIES;
      case EMPTY -> AssetAllocationRegion.UNCLASSIFIED;
      default -> AssetAllocationRegion.INTERNATIONAL_EQUITIES;
    };
  }
}
