package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.domain.model.EquityMarketCapitalizationStock;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.equitymarketcapitalization.REquityMarketCapitalization;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.graphql.MultipleSMRepository;
import com.fintex.ce.adapter.cache.repository.equitymarketcapitalization.EquityMarketCapitalizationRepository;
import com.fintex.ce.adapter.cache.repository.equitymarketcapitalization.EquityMarketCapitalizationStockRepository;
import com.fintex.ce.adapter.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_EMC_EMC_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_EMC_SBV_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
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
public class EquityMarketCapitalizationCacheStorage
    extends
      MultipleCacheStorageAbstract<EquityMarketCapitalization, EquityMarketCapitalization, EquityMarketCapitalization, EquityMarketCapitalizationStock, REquityMarketCapitalization> {

  static final Map<EquityMarketCapType, BigDecimal> DEFAULT_MAP;

  static {
    DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(EquityMarketCapType.values()).collect(toMap(type -> type, type -> ZERO)));
  }

  private final EquityMarketCapitalizationStockRepository stockRepository;

  public EquityMarketCapitalizationCacheStorage(
      MultipleSMRepository<EquityMarketCapitalization, EquityMarketCapitalization, EquityMarketCapitalization, EquityMarketCapitalizationStock> smRepo,
      CacheEntityMapper<EquityMarketCapitalization, REquityMarketCapitalization> mapper,
      EquityMarketCapitalizationRepository fundCanadaCacheRepo,
      EquityMarketCapitalizationRepository etfCanadaCacheRepo,
      EquityMarketCapitalizationRepository etfUsCacheRepo,
      EquityMarketCapitalizationStockRepository stockRepository,
      CacheStatisticService cacheStatisticService) {
    super(
        smRepo, mapper, mapper, mapper, null,
        fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
        null, cacheStatisticService, EQUITY_MARKET_CAPITALIZATION);
    this.stockRepository = stockRepository;
  }

  @Override
  public Map<Holding, Map<EquityMarketCapType, BigDecimal>> load(final List<Holding> holdings,
      final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    final Map<Holding, Map<EquityMarketCapType, BigDecimal>> map = new HashMap<>();
    map.putAll(mapForNonStock(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNonStock(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
    map.putAll(mapForNonStock(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNonStock(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNonStock(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNonStock(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForNonStock(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()),
        warnings));
    map.putAll(mapForStocks(holdings, warnings));
    return map;
  }

  public Map<Holding, Map<EquityMarketCapType, BigDecimal>> mapForStocks(final List<Holding> holdings,
      final List<Warning> warnings) {
    final List<StockHolding> stockHoldings = filterHoldings(holdings, STOCK_PREDICATE);
    final Map<StockHolding, EquityMarketCapitalization> responseMap = loadStockCapitalizations(stockHoldings);
    return convertRatingsForStocks(responseMap, warnings);
  }

  private Map<StockHolding, EquityMarketCapitalization> loadStockCapitalizations(final List<StockHolding> holdings) {
    Map<StockHolding, EquityMarketCapitalization> result = new HashMap<>();
    for (StockHolding holding : holdings) {
      var cached = stockRepository.findAllByHoldingId(holding.generateUserIdentifier());
      if (!cached.isEmpty()) {
        var stock = cached.get(0);
        EquityMarketCapitalization domain = new EquityMarketCapitalization();
        domain.setHoldingId(stock.getHoldingId());
        domain.setProvider(stock.getProvider());
        domain.setProviders(stock.getProviders());
        if (stock.getStyleBox() != null) {
          domain.setRatings(Map.of(stock.getStyleBox(), ONE));
        }
        result.put(holding, domain);
      }
    }
    return result;
  }

  public Map<Holding, Map<EquityMarketCapType, BigDecimal>> convertRatingsForStocks(
      final Map<StockHolding, EquityMarketCapitalization> responseMap,
      final List<Warning> warnings) {
    final Map<Holding, Map<EquityMarketCapType, BigDecimal>> result = new HashMap<>(responseMap.size());
    responseMap.forEach((holding, res) -> {
      Map<String, BigDecimal> ratings = res.getRatings();
      String styleBox = (ratings != null && !ratings.isEmpty()) ? ratings.keySet().iterator().next() : null;
      if (isNull(styleBox)) {
        warnings.add(new Warning(
            holding.generateUserIdentifier(),
            WRN_EMC_SBV_001.getMessage(),
            WRN_EMC_SBV_001.name()));
        return;
      }
      final EquityMarketCapType type = getEquityMarketCapType(holding, styleBox, warnings);
      if (!isNull(type)) {
        result.put(holding, overrideDefaultValues(DEFAULT_MAP, Map.of(type, ONE)));
      }
    });
    return result;
  }

  /**
   * Mapper for non stock holdings
   *
   * @param holdings
   *          holdings
   * @param warnings
   *          warnings
   * @param <H>
   *          holding type
   * @return mapped response
   */
  public <H extends Holding> Map<Holding, Map<EquityMarketCapType, BigDecimal>> mapForNonStock(
      final Map<H, EquityMarketCapitalization> holdings,
      final List<Warning> warnings) {
    return holdings.entrySet().stream().collect(
        toMap(
            Map.Entry::getKey,
            e -> convertRatings(e, warnings)));
  }

  public <H extends Holding> Map<EquityMarketCapType, BigDecimal> convertRatings(
      final Map.Entry<H, EquityMarketCapitalization> entry,
      final List<Warning> warnings) {
    final Map<String, BigDecimal> ratingsRaw = entry.getValue().getRatings();
    if (CollectionUtils.isEmpty(ratingsRaw)) {
      warnings.add(WRN_EMC_EMC_001.warning(entry.getKey()));
      return DEFAULT_MAP;
    }
    return mapRatingsToRequiredFormat(entry.getKey(), ratingsRaw, warnings);
  }

  /**
   * Converts rating from string to required Enum type for non Stocks holdings
   *
   * @param holding
   *          holding
   * @param ratingsRaw
   *          ratings in string type
   * @param warnings
   *          warnings
   * @return converted ratings map
   */
  public Map<EquityMarketCapType, BigDecimal> mapRatingsToRequiredFormat(final Holding holding,
      final Map<String, BigDecimal> ratingsRaw,
      final List<Warning> warnings) {
    final Map<EquityMarketCapType, BigDecimal> ratings = new HashMap<>(ratingsRaw.size());
    ratingsRaw.forEach((typeStr, value) -> {
      final EquityMarketCapType type = getEquityMarketCapType(holding, typeStr, warnings);
      if (!isNull(type)) {
        ratings.put(type, value);
      }
    });
    return overrideDefaultValues(DEFAULT_MAP, ratings);
  }

  public EquityMarketCapType getEquityMarketCapType(final Holding holding,
      final String typeStr,
      final List<Warning> warnings) {
    final EquityMarketCapType type = EquityMarketCapType.of(typeStr);
    if (isNull(type)) {
      warnings.add(WRN_UNKNOWN_001.warning(holding, typeStr, "FDS Equity Market Capitalization"));
    }
    return type;
  }

}
