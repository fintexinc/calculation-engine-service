package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.Country;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.calculation.CountryRegionType;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.REquityCountryAllocation;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.graphql.MultipleSMRepository;
import com.fintex.ce.adapter.cache.repository.EquityCountryAllocationRepository;
import com.fintex.ce.adapter.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.CountryAllocationMappingService;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.CollectorUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static com.fintex.ce.domain.enumeration.Country.EMPTY;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_RRC_ECE_001;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.domain.enumeration.calculation.CountryRegionType.CANADA;
import static com.fintex.ce.domain.enumeration.calculation.CountryRegionType.INTERNATIONAL_DEVELOPED;
import static com.fintex.ce.domain.enumeration.calculation.CountryRegionType.UNITED_STATES;
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
    extends
      MultipleCacheStorageAbstract<EquityCountryAllocation, EquityCountryAllocation, EquityCountryAllocation, EquityCountryAllocation, REquityCountryAllocation> {

  private final CountryAllocationMappingService countryAllocationService;
  private final BusinessCountryCacheStorage businessCountryCacheStorage;

  public EquityCountryAllocationCacheStorage(
      MultipleSMRepository<EquityCountryAllocation, EquityCountryAllocation, EquityCountryAllocation, EquityCountryAllocation> smRepo,
      CacheEntityMapper<EquityCountryAllocation, REquityCountryAllocation> mapper,
      EquityCountryAllocationRepository fundCanadaCacheRepo,
      EquityCountryAllocationRepository etfCanadaCacheRepo,
      EquityCountryAllocationRepository etfUsCacheRepo,
      CountryAllocationMappingService countryAllocationService,
      CacheStatisticService cacheStatisticService,
      BusinessCountryCacheStorage businessCountryCacheStorage) {
    super(
        smRepo, mapper, mapper, mapper, mapper,
        fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
        null, cacheStatisticService, EQUITY_COUNTRY_ALLOCATIONS);
    this.countryAllocationService = countryAllocationService;
    this.businessCountryCacheStorage = businessCountryCacheStorage;
  }

  @Override
  public Map<Holding, Map<CountryRegionType, BigDecimal>> load(final List<Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    return load(holdings, providers, warnings, false);
  }

  public Map<Holding, Map<CountryRegionType, BigDecimal>> loadWithDataProvidersCheck(final List<Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings) {
    return load(holdings, providers, warnings, true);
  }

  public Map<Holding, Map<CountryRegionType, BigDecimal>> load(final List<Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings, final boolean needToCheckDataProvidersFromResponse) {
    final Map<Holding, Map<CountryRegionType, BigDecimal>> result = new HashMap<>();

    final Map<FundSeriesHolding, EquityCountryAllocation> mutualFunds = loadBenchOfFundCanada(filterHoldings(holdings,
        CANADA_MUTUAL_PREDICATE), List.of());
    final Map<CanadaPooledFundHolding, EquityCountryAllocation> canadaPooledFunds = loadCanadaPooledFunds(
        filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of());
    final Map<UsMutualFundHolding, EquityCountryAllocation> usMutualFunds = loadUsMutualFunds(filterHoldings(holdings,
        US_MUTUAL_FUND_PREDICATE), List.of());
    final Map<CanadaHedgeFundHolding, EquityCountryAllocation> canadaHedgeFunds = loadCanadaHedgeFunds(filterHoldings(
        holdings, CANADA_HEDGE_FUND_PREDICATE), List.of());
    final Map<EtfHolding, EquityCountryAllocation> etfUs = loadForBenchOfEtfUs(filterHoldings(holdings,
        US_ETF_PREDICATE), List.of());
    final Map<EtfHolding, EquityCountryAllocation> etfCanada = loadForBenchOfEtfCanada(filterHoldings(holdings,
        CANADA_ETF_PREDICATE), List.of());
    final Map<BenchmarkIndexHolding, EquityCountryAllocation> benchmark = loadForBenchOfBenchmarks(filterHoldings(
        holdings, BENCHMARKS_PREDICATE), List.of());

    if (needToCheckDataProvidersFromResponse) {
      dataProviderCheckValidation(providers, mutualFunds.values(), EquityCountryAllocation::getProvider,
          clearAssetAllocation());
      dataProviderCheckValidation(providers, canadaPooledFunds.values(), EquityCountryAllocation::getProvider,
          clearAssetAllocation());
      dataProviderCheckValidation(providers, usMutualFunds.values(), EquityCountryAllocation::getProvider,
          clearAssetAllocation());
      dataProviderCheckValidation(providers, canadaHedgeFunds.values(), EquityCountryAllocation::getProvider,
          clearAssetAllocation());
      dataProviderCheckValidation(providers, etfUs.values(), EquityCountryAllocation::getProvider,
          clearAssetAllocation());
      dataProviderCheckValidation(providers, etfCanada.values(), EquityCountryAllocation::getProvider,
          clearAssetAllocation());
      dataProviderCheckValidation(providers, benchmark.values(), EquityCountryAllocation::getProvider,
          clearAssetAllocation());
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

  public BiFunction<EquityCountryAllocation, Object, EquityCountryAllocation> clearAssetAllocation() {
    return (t, u) -> {
      t.setAllocations(Map.of());
      return t;
    };
  }

  public Map<Holding, Map<CountryRegionType, BigDecimal>> mapForStocks(final List<Holding> holdings,
      final List<DataProvider> providers,
      final boolean needToCheckDataProvidersFromResponse,
      final List<Warning> warnings) {
    final Map<Holding, Country> countries = businessCountryCacheStorage.loadBusinessCountries(filterHoldings(holdings,
        STOCK_PREDICATE),
        providers,
        needToCheckDataProvidersFromResponse,
        warnings);

    return countries.entrySet().stream()
        .filter(c -> Objects.nonNull(c.getValue()))
        .filter(c -> !EMPTY.equals(c.getValue()))
        .collect(
            CollectorUtils.toMap(
                Map.Entry::getKey,
                e -> Map.of(defineType(e.getValue()), BigDecimal.ONE)));
  }

  private CountryRegionType defineType(final Country country) {
    return switch (country) {
      case CAN -> CANADA;
      case USA -> UNITED_STATES;
      default -> INTERNATIONAL_DEVELOPED;
    };
  }

  public <H extends Holding> Map<Holding, Map<CountryRegionType, BigDecimal>> mapForNoneStock(
      final Map<H, EquityCountryAllocation> holdings,
      final List<Warning> warnings) {
    final Map<Holding, Map<String, BigDecimal>> collect = holdings.entrySet().stream().collect(
        toMap(
            Map.Entry::getKey,
            e -> e.getValue().getAllocations()));
    return countryAllocationService.mapToCountryRegions(collect, warnings, WRN_RRC_ECE_001);
  }

}
