package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.Country;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.calculation.CountryRegionType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.REquityCountryAllocation;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.EquityCountryAllocationSMRepository;
import com.fintex.ce.repository.redis.EquityCountryAllocationRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.CountryAllocationMappingService;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.CollectorUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static com.fintex.ce.config.enumeration.Country.EMPTY;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_RRC_ECE_001;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.config.enumeration.calculation.CountryRegionType.CANADA;
import static com.fintex.ce.config.enumeration.calculation.CountryRegionType.INTERNATIONAL_DEVELOPED;
import static com.fintex.ce.config.enumeration.calculation.CountryRegionType.UNITED_STATES;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static com.fintex.ce.util.validation.DataProviderRequestHandlingValidator.dataProviderCheckValidation;
import static java.util.stream.Collectors.toMap;

@Service
public class EquityCountryAllocationCacheStorage
        extends MultipleCacheStorageAbstract<REquityCountryAllocation, REquityCountryAllocation, REquityCountryAllocation, RedisId> {

    private final CountryAllocationMappingService countryAllocationService;
    private final BusinessCountryCacheStorage businessCountryCacheStorage;

    public EquityCountryAllocationCacheStorage(EquityCountryAllocationSMRepository fdsRepo,
                                               EquityCountryAllocationRepository fundCanadaCacheRepo,
                                               EquityCountryAllocationRepository etfCanadaCacheRepo,
                                               EquityCountryAllocationRepository etfUsCacheRepo,
                                               CountryAllocationMappingService countryAllocationService,
                                               CacheStatisticService cacheStatisticService,
                                               BusinessCountryCacheStorage businessCountryCacheStorage) {
        super(
                fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
                null, cacheStatisticService, EQUITY_COUNTRY_ALLOCATIONS
        );
        this.countryAllocationService = countryAllocationService;
        this.businessCountryCacheStorage = businessCountryCacheStorage;
    }

    @Override
    public Map<Holding, Map<CountryRegionType, BigDecimal>> load(final List<Holding> holdings, final List<DataProvider> providers,
                                                                 final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        return load(holdings, providers, warnings, false);
    }

    public Map<Holding, Map<CountryRegionType, BigDecimal>> loadWithDataProvidersCheck(final List<Holding> holdings, final List<DataProvider> providers,
                                                                                       final List<Warning> warnings) {
        return load(holdings, providers, warnings, true);
    }

    Map<Holding, Map<CountryRegionType, BigDecimal>> load(final List<Holding> holdings, final List<DataProvider> providers,
                                                          final List<Warning> warnings, final boolean needToCheckDataProvidersFromResponse) {
        final Map<Holding, Map<CountryRegionType, BigDecimal>> result = new HashMap<>();

        final Map<FundSeriesHolding, REquityCountryAllocation> mutualFunds = loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of());
        final Map<CanadaPooledFundHolding, REquityCountryAllocation> canadaPooledFunds = loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of());
        final Map<UsMutualFundHolding, REquityCountryAllocation> usMutualFunds = loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of());
        final Map<CanadaHedgeFundHolding, REquityCountryAllocation> canadaHedgeFunds = loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of());
        final Map<EtfHolding, REquityCountryAllocation> etfUs = loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of());
        final Map<EtfHolding, REquityCountryAllocation> etfCanada = loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of());
        final Map<BenchmarkIndexHolding, REquityCountryAllocation> benchmark = loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of());

        if (needToCheckDataProvidersFromResponse) {
            dataProviderCheckValidation(providers, mutualFunds.values(), clearAssetAllocation());
            dataProviderCheckValidation(providers, canadaPooledFunds.values(), clearAssetAllocation());
            dataProviderCheckValidation(providers, usMutualFunds.values(), clearAssetAllocation());
            dataProviderCheckValidation(providers, canadaHedgeFunds.values(), clearAssetAllocation());
            dataProviderCheckValidation(providers, etfUs.values(), clearAssetAllocation());
            dataProviderCheckValidation(providers, etfCanada.values(), clearAssetAllocation());
            dataProviderCheckValidation(providers, benchmark.values(), clearAssetAllocation());
        }

        result.putAll(mapForNoneStock(mutualFunds, warnings));
        result.putAll(mapForNoneStock(canadaPooledFunds, warnings));
        result.putAll(mapForNoneStock(usMutualFunds, warnings));
        result.putAll(mapForNoneStock(canadaHedgeFunds, warnings));
        result.putAll(mapForNoneStock(etfUs, warnings));
        result.putAll(mapForNoneStock(etfCanada, warnings));
        result.putAll(mapForNoneStock(benchmark, warnings));
        result.putAll(mapForStocks(holdings, providers, needToCheckDataProvidersFromResponse, warnings));
        return result;
    }

    BiFunction<REquityCountryAllocation, Object, REquityCountryAllocation> clearAssetAllocation() {
        return (t, u) -> {
            t.setAllocations(Map.of());
            return t;
        };
    }

    Map<Holding, Map<CountryRegionType, BigDecimal>> mapForStocks(final List<Holding> holdings,
                                                                  final List<DataProvider> providers,
                                                                  final boolean needToCheckDataProvidersFromResponse,
                                                                  final List<Warning> warnings) {
        final Map<Holding, Country> countries = businessCountryCacheStorage.loadBusinessCountries(filterHoldings(holdings, STOCK_PREDICATE),
                providers,
                needToCheckDataProvidersFromResponse,
                warnings);

        return countries.entrySet().stream()
                .filter(c -> Objects.nonNull(c.getValue()))
                .filter(c -> !EMPTY.equals(c.getValue()))
                .collect(
                        CollectorUtils.toMap(
                                Map.Entry::getKey,
                                e -> Map.of(defineType(e.getValue()), BigDecimal.ONE)
                        )
                );
    }

    private CountryRegionType defineType(final Country country) {
        return switch (country) {
            case CAN -> CANADA;
            case USA -> UNITED_STATES;
            default -> INTERNATIONAL_DEVELOPED;
        };
    }

    <H extends Holding> Map<Holding, Map<CountryRegionType, BigDecimal>> mapForNoneStock(final Map<H, REquityCountryAllocation> holdings,
                                                                                         final List<Warning> warnings) {
        final Map<Holding, Map<String, BigDecimal>> collect = holdings.entrySet().stream().collect(
                toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getAllocations()
                )
        );
        return countryAllocationService.mapToCountryRegions(collect, warnings, WRN_RRC_ECE_001);
    }

}
