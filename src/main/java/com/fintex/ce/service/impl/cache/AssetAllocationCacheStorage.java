package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.Country;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.cache.CacheNameEntity;
import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.calculation.AssetAllocationDataDTO;
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
import com.fintex.ce.repository.graphql.query.AssetAllocationSMRepository;
import com.fintex.ce.repository.redis.assetallocation.AssetAllocationRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.CollectorUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AssetAllocationCacheStorage extends MultipleCacheStorageAbstract<RAssetAllocation, RAssetAllocation, RAssetAllocation, RAssetAllocation> {

    private final BusinessCountryCacheStorage businessCountryCacheStorage;

    @Autowired
    public AssetAllocationCacheStorage(final AssetAllocationSMRepository queryRepository,
                                       final AssetAllocationRepository assetAllocationRepository,
                                       final BusinessCountryCacheStorage businessCountryCacheStorage,
                                       final CacheStatisticService cacheStatisticService) {
        super(queryRepository, assetAllocationRepository, assetAllocationRepository,
                assetAllocationRepository, assetAllocationRepository, cacheStatisticService, CacheNameEntity.ASSET_ALLOCATION);
        this.businessCountryCacheStorage = businessCountryCacheStorage;
    }

    @Override
    public AssetAllocationDataDTO load(final List<Holding> holdings, final List<DataProvider> providers, final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        return load(holdings, providers, false, warnings);
    }

    public AssetAllocationDataDTO loadWithDataProvidesCheck(final List<Holding> holdings,
                                                            final List<DataProvider> dataProviders,
                                                            final List<Warning> warnings) {
        return load(holdings, dataProviders, true, warnings);
    }

    AssetAllocationDataDTO load(final List<Holding> holdings,
                                final List<DataProvider> dataProviders,
                                final boolean needToCheckDataProvidersFromResponse,
                                final List<Warning> warnings) {
        final Map<EtfHolding, RAssetAllocation> etfUsFdsResponse = loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of());
        final Map<EtfHolding, RAssetAllocation> etfCanadaFdsResponse = loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of());
        final Map<FundSeriesHolding, RAssetAllocation> mutualFundFdsResponse = loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), dataProviders);
        final Map<BenchmarkIndexHolding, RAssetAllocation> benchmarkIndexFdsResponse = loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), dataProviders);
        final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> stocksFdsResponse = loadStocks(holdings, dataProviders, needToCheckDataProvidersFromResponse, warnings);
        final Map<CanadaPooledFundHolding, RAssetAllocation> pooledFunds = loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), dataProviders);
        final Map<CanadaHedgeFundHolding, RAssetAllocation> hedgeFunds = loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), dataProviders);
        final Map<UsMutualFundHolding, RAssetAllocation> usMutualFunds = loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), dataProviders);
        final Map<FixedIncomeHolding, RAssetAllocation> fixedIncomes = loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), dataProviders);
        final Map<SmaHolding, RAssetAllocation> separatelyManagedAccounts = loadBenchOfSeparatelyManagedAccounts(filterHoldings(holdings, SEPARATELY_MANAGED_ACCOUNT_PREDICATE), dataProviders);

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

    Map<Holding, Map<AssetAllocationRegion, BigDecimal>> loadStocks(final List<Holding> holdings,
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
                                e -> Map.of(defineType(e.getValue()), BigDecimal.ONE)
                        )
                );
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
