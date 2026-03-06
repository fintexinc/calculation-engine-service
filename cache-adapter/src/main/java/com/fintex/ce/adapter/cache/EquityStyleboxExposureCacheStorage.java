package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.core.CacheStorageAbstract;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.calculation.EquityStyleboxType;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.REquityStyleboxExposure;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.adapter.cache.repository.EquityStyleboxAllocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.constant.CacheNameEntity.EQUITY_STYLEBOX_ALLOCATION;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_ES_ESE_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class EquityStyleboxExposureCacheStorage
    extends CacheStorageAbstract<EquityStyleboxExposure, REquityStyleboxExposure, Map<Holding, Map<EquityStyleboxType, BigDecimal>>> {

  public static final Map<EquityStyleboxType, BigDecimal> DEFAULT_MAP;

  static {
    DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(EquityStyleboxType.values()).collect(toMap(type -> type, type -> ZERO)));
  }

  public EquityStyleboxExposureCacheStorage(
      SecurityDataPort<EquityStyleboxExposure> securityDataPort,
      CacheEntityMapper<EquityStyleboxExposure, REquityStyleboxExposure> mapper,
      EquityStyleboxAllocationRepository equityStyleboxAllocationRepository,
      CacheStatisticService cacheStatisticService) {
    super(securityDataPort, mapper, equityStyleboxAllocationRepository, cacheStatisticService, EQUITY_STYLEBOX_ALLOCATION);
  }

  @Override
  public Map<Holding, Map<EquityStyleboxType, BigDecimal>> load(final List<? extends Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {

    Map<Holding, Map<EquityStyleboxType, BigDecimal>> map = new HashMap<>();
    map.putAll(mapForNoneStock(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNoneStock(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNoneStock(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
    map.putAll(mapForNoneStock(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNoneStock(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNoneStock(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNoneStock(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()),
        warnings));
    return map;
  }

  public <H extends Holding> Map<Holding, Map<EquityStyleboxType, BigDecimal>> mapForNoneStock(
      final Map<H, EquityStyleboxExposure> holdings,
      final List<Warning> warnings) {
    return holdings.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> equityStyleboxExposureMapper(e,
        warnings)));
  }

  public <H extends Holding> Map<EquityStyleboxType, BigDecimal> equityStyleboxExposureMapper(
      final Map.Entry<H, EquityStyleboxExposure> entry,
      final List<Warning> warnings) {
    final Map<EquityStyleboxType, BigDecimal> map = new HashMap<>(DEFAULT_MAP);
    if (CollectionUtils.isEmpty(entry.getValue().getBoxValues())) {
      warnings.add(WRN_ES_ESE_001.warning(entry.getKey()));
      return map;
    }
    entry.getValue().getBoxValues().forEach((typeStr, value) -> {
      final EquityStyleboxType type = EquityStyleboxType.of(typeStr);
      if (type == null) {
        warnings.add(WRN_UNKNOWN_001.warning(entry.getKey(), typeStr, "FDS Get Equity Stylebox Exposure"));
      } else {
        map.put(type, value);
      }
    });
    return map;
  }
}
