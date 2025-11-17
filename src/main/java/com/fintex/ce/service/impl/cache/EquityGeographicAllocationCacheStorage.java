package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.Country;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.calculation.GeographicRegionType;
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
import com.fintex.ce.service.GeographicAllocationMappingService;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.CollectorUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fintex.ce.config.enumeration.Country.EMPTY;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_RRC_EGE_001;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.config.enumeration.calculation.GeographicRegionType.CANADA;
import static com.fintex.ce.config.enumeration.calculation.GeographicRegionType.OTHER;
import static com.fintex.ce.config.enumeration.calculation.GeographicRegionType.US;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static java.util.stream.Collectors.toMap;

@Service
public class EquityGeographicAllocationCacheStorage
        extends MultipleCacheStorageAbstract<REquityCountryAllocation, REquityCountryAllocation, REquityCountryAllocation, RedisId> {

    private final GeographicAllocationMappingService geographicAllocationService;
    private final BusinessCountryCacheStorage businessCountryCacheStorage;

    public EquityGeographicAllocationCacheStorage(EquityCountryAllocationSMRepository fdsRepo,
                                                  EquityCountryAllocationRepository fundCanadaCacheRepo,
                                                  EquityCountryAllocationRepository etfCanadaCacheRepo,
                                                  EquityCountryAllocationRepository etfUsCacheRepo,
                                                  GeographicAllocationMappingService geographicAllocationService,
                                                  CacheStatisticService cacheStatisticService,
                                                  BusinessCountryCacheStorage businessCountryCacheStorage) {
        super(
                fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
                null, cacheStatisticService, EQUITY_COUNTRY_ALLOCATIONS
        );
        this.geographicAllocationService = geographicAllocationService;
        this.businessCountryCacheStorage = businessCountryCacheStorage;
    }

    @Override
    public Map<Holding, Map<GeographicRegionType, BigDecimal>> load(final List<Holding> holdings, final List<DataProvider> providers,
                                                                    final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        final Map<Holding, Map<GeographicRegionType, BigDecimal>> result = new HashMap<>();

        final Map<FundSeriesHolding, REquityCountryAllocation> mutualFunds = loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of());
        final Map<CanadaPooledFundHolding, REquityCountryAllocation> canadaPooledFunds = loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of());
        final Map<UsMutualFundHolding, REquityCountryAllocation> usMutualFunds = loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of());
        final Map<CanadaHedgeFundHolding, REquityCountryAllocation> canadaHedgeFunds = loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of());
        final Map<EtfHolding, REquityCountryAllocation> etfUs = loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of());
        final Map<EtfHolding, REquityCountryAllocation> etfCanada = loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of());
        final Map<BenchmarkIndexHolding, REquityCountryAllocation> benchmark = loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of());

        result.putAll(mapForNoneStock(mutualFunds, warnings));
        result.putAll(mapForNoneStock(canadaPooledFunds, warnings));
        result.putAll(mapForNoneStock(usMutualFunds, warnings));
        result.putAll(mapForNoneStock(canadaHedgeFunds, warnings));
        result.putAll(mapForNoneStock(etfUs, warnings));
        result.putAll(mapForNoneStock(etfCanada, warnings));
        result.putAll(mapForNoneStock(benchmark, warnings));
        result.putAll(mapForStocks(holdings, providers, warnings));
        return result;
    }

    private Map<Holding, Map<GeographicRegionType, BigDecimal>> mapForStocks(final List<Holding> holdings,
                                                                             final List<DataProvider> providers,
                                                                             final List<Warning> warnings) {
        final Map<Holding, Country> countries = businessCountryCacheStorage.loadBusinessCountries(filterHoldings(holdings, STOCK_PREDICATE),
                providers,
                false,
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

    private GeographicRegionType defineType(final Country country) {
        return switch (country) {
            case CAN -> CANADA;
            case USA -> US;
            default -> OTHER;
        };
    }

    private <H extends Holding> Map<Holding, Map<GeographicRegionType, BigDecimal>> mapForNoneStock(final Map<H, REquityCountryAllocation> holdings,
                                                                                                    final List<Warning> warnings) {
        final Map<Holding, Map<String, BigDecimal>> collect = holdings.entrySet().stream().collect(
                toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getAllocations()
                )
        );
        return geographicAllocationService.mapToGeographicRegions(collect, warnings, WRN_RRC_EGE_001);
    }

}
