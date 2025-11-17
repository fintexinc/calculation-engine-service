package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.calculation.EquitySectorAllocationType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.equitysector.REquitySector;
import com.fintex.ce.model.redis.equitysector.REquitySectorStock;
import com.fintex.ce.repository.graphql.query.EquitySectorSMRepository;
import com.fintex.ce.repository.redis.equitysector.EquitySectorRepository;
import com.fintex.ce.repository.redis.equitysector.EquitySectorStockRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_ES_ESA_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_ES_SN_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_SECTOR;
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
        extends MultipleCacheStorageAbstract<REquitySector, REquitySector, REquitySector, REquitySectorStock> {

    static final Map<EquitySectorAllocationType, BigDecimal> DEFAULT_MAP;

    static {
        DEFAULT_MAP = Collections.unmodifiableMap(
                Stream.of(EquitySectorAllocationType.values()).collect(toMap(type -> type, type -> ZERO))
        );
    }

    public EquitySectorCacheStorage(EquitySectorSMRepository fdsRepo,
                                    EquitySectorRepository fundCanadaCacheRepo,
                                    EquitySectorRepository etfCanadaCacheRepo,
                                    EquitySectorRepository etfUsCacheRepo,
                                    EquitySectorStockRepository stockRepository,
                                    CacheStatisticService cacheStatisticService) {
        super(
                fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
                stockRepository, cacheStatisticService, EQUITY_SECTOR
        );
    }

    @Override
    public Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> load(final List<Holding> holdings, final List<DataProvider> providers,
                                                                          final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> map = new HashMap<>();
        map.putAll(mapForNoneStock(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapForStocks(holdings, warnings));
        return map;
    }

    Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> mapForStocks(final List<Holding> holdings, final List<Warning> warnings) {
        final Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> map = new HashMap<>();
        final Map<StockHolding, REquitySectorStock> stocks = loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE), List.of());
        stocks.forEach((stockHolding, equitySectorStock) -> {
            if (isNull(equitySectorStock.getSectorName())) {
                warnings.add(new Warning(
                        stockHolding.generateUserIdentifier(),
                        WRN_ES_SN_001.getMessage(),
                        WRN_ES_SN_001.name()));
                return;
            }
            final EquitySectorAllocationType type = EquitySectorAllocationType.of(equitySectorStock.getSectorName());
            if (type == null) {
                throw WRN_UNKNOWN_001.error(stockHolding, equitySectorStock.getSectorName(), "FDS Get Sector Name");
            }
            map.put(stockHolding, overrideDefaultValues(DEFAULT_MAP, Map.of(type, ONE)));
        });
        return map;
    }

    <H extends Holding> Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> mapForNoneStock(final Map<H, REquitySector> holdings,
                                                                                                  final List<Warning> warnings) {
        return holdings.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> equitySectorAllocationMapper(e, warnings)));
    }

    <H extends Holding> Map<EquitySectorAllocationType, BigDecimal> equitySectorAllocationMapper(final Map.Entry<H, REquitySector> entry,
                                                                                                 final List<Warning> warnings) {
        final Map<EquitySectorAllocationType, BigDecimal> map = new EnumMap<>(DEFAULT_MAP);
        if (CollectionUtils.isEmpty(entry.getValue().getAllocations())) {
            warnings.add(WRN_ES_ESA_001.warning(entry.getKey()));
            return map;
        }
        entry.getValue().getAllocations().forEach((typeStr, value) -> {
            final EquitySectorAllocationType type = EquitySectorAllocationType.of(typeStr);
            if (type == null) {
                warnings.add(WRN_UNKNOWN_001.warning(entry.getKey(), typeStr, "FDS Get Equity Sector Allocations"));
            } else {
                map.put(type, value);
            }
        });
        return map;
    }

}
