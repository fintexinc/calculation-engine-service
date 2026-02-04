package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.calculation.CountryRegionType;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.RCountryExposure;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.graphql.MultipleSMRepository;
import com.fintex.ce.adapter.cache.repository.CountryExposureRepository;
import com.fintex.ce.adapter.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.CountryAllocationMappingService;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FICQ_BCE_001;
import static com.fintex.ce.constant.CacheNameEntity.COUNTRY_EXPOSURE;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static java.util.stream.Collectors.toMap;

@Service
public class CountryExposureCacheStorage
    extends
      MultipleCacheStorageAbstract<CountryExposure, CountryExposure, CountryExposure, CountryExposure, RCountryExposure> {

  private final CountryAllocationMappingService countryAllocationMappingService;

  public CountryExposureCacheStorage(
      MultipleSMRepository<CountryExposure, CountryExposure, CountryExposure, CountryExposure> smRepo,
      CacheEntityMapper<CountryExposure, RCountryExposure> mapper,
      CountryExposureRepository fundCanadaCacheRepo,
      CountryExposureRepository etfCanadaCacheRepo,
      CountryExposureRepository etfUsCacheRepo,
      CacheStatisticService cacheStatisticService,
      CountryAllocationMappingService countryAllocationMappingService) {
    super(
        smRepo, mapper, mapper, mapper, mapper,
        fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
        null, cacheStatisticService, COUNTRY_EXPOSURE);
    this.countryAllocationMappingService = countryAllocationMappingService;
  }

  @Override
  public Map<Holding, Map<CountryRegionType, BigDecimal>> load(final List<Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    Map<Holding, Map<CountryRegionType, BigDecimal>> map = new HashMap<>();
    map.putAll(mapper(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
    map.putAll(mapper(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()), warnings));
    map.putAll(mapper(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()), warnings));
    map.putAll(mapper(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()), warnings));
    map.putAll(mapper(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapper(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapper(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
    map.putAll(addGics(filterHoldings(holdings, GIC_PREDICATE)));
    map.putAll(mapper(loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), List.of()), warnings));
    return map;
  }

  public Map<Holding, Map<CountryRegionType, BigDecimal>> addGics(final List<Holding> holdings) {
    final HashMap<Holding, Map<CountryRegionType, BigDecimal>> result = new HashMap<>();
    for (final Holding holding : holdings) {
      final GicHolding gic = (GicHolding) holding;
      if (!gic.isLessThanOneYearOld()) {
        result.put(holding, Map.of(CountryRegionType.CANADA, BigDecimal.ONE));
      }
    }
    return result;
  }

  public <H extends Holding> Map<Holding, Map<CountryRegionType, BigDecimal>> mapper(
      final Map<H, CountryExposure> holdings,
      final List<Warning> warnings) {
    final Map<Holding, Map<String, BigDecimal>> mappedHoldings = holdings.entrySet().stream().collect(toMap(
        Map.Entry::getKey, e -> e.getValue().getAllocations()));
    return countryAllocationMappingService.mapToCountryRegions(mappedHoldings, warnings, WRN_FICQ_BCE_001);
  }

}
