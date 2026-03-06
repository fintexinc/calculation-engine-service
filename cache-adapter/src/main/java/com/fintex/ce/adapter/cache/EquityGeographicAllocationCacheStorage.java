package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.Country;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.calculation.GeographicRegionType;
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
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.adapter.cache.repository.EquityCountryAllocationRepository;
import com.fintex.ce.service.GeographicAllocationMappingService;
import com.fintex.ce.adapter.cache.core.CacheStorageAbstract;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.CollectorUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fintex.ce.domain.enumeration.Country.EMPTY;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_RRC_EGE_001;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.domain.enumeration.calculation.GeographicRegionType.CANADA;
import static com.fintex.ce.domain.enumeration.calculation.GeographicRegionType.OTHER;
import static com.fintex.ce.domain.enumeration.calculation.GeographicRegionType.US;
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
@Qualifier("equityGeographic")
public class EquityGeographicAllocationCacheStorage
    extends CacheStorageAbstract<EquityCountryAllocation, REquityCountryAllocation, Map<Holding, Map<GeographicRegionType, BigDecimal>>> {

  private final GeographicAllocationMappingService geographicAllocationService;
  private final BusinessCountryCacheStorage businessCountryCacheStorage;

  public EquityGeographicAllocationCacheStorage(
      SecurityDataPort<EquityCountryAllocation> securityDataPort,
      CacheEntityMapper<EquityCountryAllocation, REquityCountryAllocation> mapper,
      EquityCountryAllocationRepository equityCountryAllocationRepository,
      GeographicAllocationMappingService geographicAllocationService,
      CacheStatisticService cacheStatisticService,
      BusinessCountryCacheStorage businessCountryCacheStorage) {
    super(securityDataPort, mapper, equityCountryAllocationRepository, cacheStatisticService, EQUITY_COUNTRY_ALLOCATIONS);
    this.geographicAllocationService = geographicAllocationService;
    this.businessCountryCacheStorage = businessCountryCacheStorage;
  }

  @Override
  public Map<Holding, Map<GeographicRegionType, BigDecimal>> load(final List<? extends Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    final Map<Holding, Map<GeographicRegionType, BigDecimal>> result = new HashMap<>();

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

  private Map<Holding, Map<GeographicRegionType, BigDecimal>> mapForStocks(final List<? extends Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings) {
    final Map<Holding, Country> countries = businessCountryCacheStorage.loadBusinessCountries(filterHoldings(holdings,
        STOCK_PREDICATE),
        providers,
        false,
        warnings);

    return countries.entrySet().stream()
        .filter(c -> Objects.nonNull(c.getValue()))
        .filter(c -> !EMPTY.equals(c.getValue()))
        .collect(
            CollectorUtils.toMap(
                Map.Entry::getKey,
                e -> Map.of(defineType(e.getValue()), BigDecimal.ONE)));
  }

  private GeographicRegionType defineType(final Country country) {
    return switch (country) {
      case CAN -> CANADA;
      case USA -> US;
      default -> OTHER;
    };
  }

  private <H extends Holding> Map<Holding, Map<GeographicRegionType, BigDecimal>> mapForNoneStock(
      final Map<H, EquityCountryAllocation> holdings,
      final List<Warning> warnings) {
    final Map<Holding, Map<String, BigDecimal>> collect = holdings.entrySet().stream().collect(
        toMap(
            Map.Entry::getKey,
            e -> e.getValue().getAllocations()));
    return geographicAllocationService.mapToGeographicRegions(collect, warnings, WRN_RRC_EGE_001);
  }

}
