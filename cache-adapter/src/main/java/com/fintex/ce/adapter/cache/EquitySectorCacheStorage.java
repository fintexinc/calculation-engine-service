package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.sm.model.domain.enumeration.EquitySectorAllocationType;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.EquitySectorStock;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.equitysector.REquitySector;
import com.fintex.ce.adapter.cache.entity.equitysector.REquitySectorStock;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.adapter.cache.repository.equitysector.EquitySectorRepository;
import com.fintex.ce.adapter.cache.repository.equitysector.EquitySectorStockRepository;
import com.fintex.ce.adapter.cache.core.CacheStorageAbstract;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_ES_ESA_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_ES_SN_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static com.fintex.ce.util.MapUtils.overrideDefaultValues;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

@Service
public class EquitySectorCacheStorage
    extends CacheStorageAbstract<EquitySector, REquitySector, Map<Holding, Map<EquitySectorAllocationType, BigDecimal>>> {

  static final Map<EquitySectorAllocationType, BigDecimal> DEFAULT_MAP;

  static {
    DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(EquitySectorAllocationType.values()).collect(toMap(type -> type, type -> ZERO)));
  }

  private final CacheEntityMapper<EquitySectorStock, REquitySectorStock> stockMapper;
  private final EquitySectorStockRepository stockRepository;

  public EquitySectorCacheStorage(
      final SecurityDataPort<EquitySector> securityDataPort,
      final CacheEntityMapper<EquitySector, REquitySector> mapper,
      final CacheEntityMapper<EquitySectorStock, REquitySectorStock> stockMapper,
      final EquitySectorRepository equitySectorRepository,
      final EquitySectorStockRepository stockRepository) {
    super(securityDataPort, mapper, equitySectorRepository, EQUITY_SECTOR);
    this.stockMapper = stockMapper;
    this.stockRepository = stockRepository;
  }

  @Override
  public Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> load(final List<? extends Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> map = new HashMap<>();
    map.putAll(mapForNoneStock(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNoneStock(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
    map.putAll(mapForNoneStock(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNoneStock(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNoneStock(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNoneStock(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNoneStock(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForStocks(holdings, warnings));
    return map;
  }

  public Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> mapForStocks(final List<? extends Holding> holdings,
      final List<Warning> warnings) {
    final Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> map = new HashMap<>();
    final List<StockHolding> stockHoldings = filterHoldings(holdings, STOCK_PREDICATE);

    // Load stock sector data directly from repository (bypassing the generic mechanism since stocks use different type)
    final Map<StockHolding, EquitySectorStock> stocks = loadStockSectors(stockHoldings);

    stocks.forEach((stockHolding, equitySectorStock) -> {
      if (isNull(equitySectorStock.getSectorName())) {
        warnings.add(new Warning(
            stockHolding.generateUserIdentifier(),
            WRN_ES_SN_001.getMessage(),
            WRN_ES_SN_001.name()));
        return;
      }
      final EquitySectorAllocationType type = resolveSectorType(equitySectorStock.getSectorName());
      if (type == null) {
        throw WRN_UNKNOWN_001.error(stockHolding, equitySectorStock.getSectorName(), "FDS Get Sector Name");
      }
      map.put(stockHolding, overrideDefaultValues(DEFAULT_MAP, Map.of(type, ONE)));
    });
    return map;
  }

  private static EquitySectorAllocationType resolveSectorType(String sectorName) {
    try {
      return EquitySectorAllocationType.valueOf(sectorName);
    } catch (IllegalArgumentException e) {
      try {
        return EquitySectorAllocationType.fromValue(sectorName);
      } catch (IllegalArgumentException e2) {
        return null;
      }
    }
  }

  private Map<StockHolding, EquitySectorStock> loadStockSectors(final List<StockHolding> holdings) {
    // For stocks, we need to use the stock-specific repository and mapper
    Map<StockHolding, EquitySectorStock> result = new HashMap<>();
    for (StockHolding holding : holdings) {
      List<REquitySectorStock> cached = stockRepository.findAllByHoldingId(holding.generateUserIdentifier());
      if (!cached.isEmpty()) {
        stockMapper.toDomain(cached.get(0)).ifPresent(domain -> result.put(holding, domain));
      }
    }
    return result;
  }

  public <H extends Holding> Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> mapForNoneStock(
      final Map<H, EquitySector> holdings,
      final List<Warning> warnings) {
    return holdings.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> equitySectorAllocationMapper(e,
        warnings)));
  }

  public <H extends Holding> Map<EquitySectorAllocationType, BigDecimal> equitySectorAllocationMapper(
      final Map.Entry<H, EquitySector> entry,
      final List<Warning> warnings) {
    final Map<EquitySectorAllocationType, BigDecimal> map = new EnumMap<>(DEFAULT_MAP);
    if (CollectionUtils.isEmpty(entry.getValue().getAllocations())) {
      warnings.add(WRN_ES_ESA_001.warning(entry.getKey()));
      return map;
    }
    map.putAll(entry.getValue().getAllocations());
    return map;
  }

}
