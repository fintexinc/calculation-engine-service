package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.calculation.EquityMarketCapType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalization;
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalizationStock;
import com.fintex.ce.repository.graphql.query.EquityMarketCapitalizationSMRepository;
import com.fintex.ce.repository.redis.equitymarketcapitalization.EquityMarketCapitalizationRepository;
import com.fintex.ce.repository.redis.equitymarketcapitalization.EquityMarketCapitalizationStockRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_EMC_EMC_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_EMC_SBV_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
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
        extends MultipleCacheStorageAbstract<REquityMarketCapitalization, REquityMarketCapitalization, REquityMarketCapitalization, REquityMarketCapitalizationStock> {

    static final Map<EquityMarketCapType, BigDecimal> DEFAULT_MAP;

    static {
        DEFAULT_MAP = Collections.unmodifiableMap(
                Stream.of(EquityMarketCapType.values()).collect(toMap(type -> type, type -> ZERO))
        );
    }

    public EquityMarketCapitalizationCacheStorage(EquityMarketCapitalizationSMRepository fdsRepo,
                                                  EquityMarketCapitalizationRepository fundCanadaCacheRepo,
                                                  EquityMarketCapitalizationRepository etfCanadaCacheRepo,
                                                  EquityMarketCapitalizationRepository etfUsCacheRepo,
                                                  EquityMarketCapitalizationStockRepository stockRepository,
                                                  CacheStatisticService cacheStatisticService) {
        super(
                fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
                stockRepository, cacheStatisticService, EQUITY_MARKET_CAPITALIZATION
        );
    }

    @Override
    public Map<Holding, Map<EquityMarketCapType, BigDecimal>> load(final List<Holding> holdings, final List<DataProvider> providers,
                                                                   final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        final Map<Holding, Map<EquityMarketCapType, BigDecimal>> map = new HashMap<>();
        map.putAll(mapForNonStock(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()), warnings));
        map.putAll(mapForNonStock(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapForNonStock(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapForNonStock(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()), warnings));
        map.putAll(mapForNonStock(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapForNonStock(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapForNonStock(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapForStocks(holdings, warnings));
        return map;
    }

    Map<Holding, Map<EquityMarketCapType, BigDecimal>> mapForStocks(final List<Holding> holdings, final List<Warning> warnings) {
        final Map<StockHolding, REquityMarketCapitalizationStock> responseMap = loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE), List.of());
        return convertRatingsForStocks(responseMap, warnings);
    }

    Map<Holding, Map<EquityMarketCapType, BigDecimal>> convertRatingsForStocks(final Map<StockHolding, REquityMarketCapitalizationStock> responseMap,
                                                                               final List<Warning> warnings) {
        final Map<Holding, Map<EquityMarketCapType, BigDecimal>> result = new HashMap<>(responseMap.size());
        responseMap.forEach((holding, res) -> {
            if (isNull(res.getStyleBox())) {
                warnings.add(new Warning(
                        holding.generateUserIdentifier(),
                        WRN_EMC_SBV_001.getMessage(),
                        WRN_EMC_SBV_001.name()));
                return;
            }
            final EquityMarketCapType type = getEquityMarketCapType(holding, res.getStyleBox(), warnings);
            if (!isNull(type)) {
                result.put(holding, overrideDefaultValues(DEFAULT_MAP, Map.of(type, ONE)));
            }
        });
        return result;
    }

    /**
     * Mapper for non stock holdings
     *
     * @param holdings holdings
     * @param warnings warnings
     * @param <H>      holding type
     * @return mapped response
     */
    <H extends Holding> Map<Holding, Map<EquityMarketCapType, BigDecimal>> mapForNonStock(final Map<H, REquityMarketCapitalization> holdings,
                                                                                          final List<Warning> warnings) {
        return holdings.entrySet().stream().collect(
                toMap(
                        Map.Entry::getKey,
                        e -> convertRatings(e, warnings)
                )
        );
    }

    <H extends Holding> Map<EquityMarketCapType, BigDecimal> convertRatings(final Map.Entry<H, REquityMarketCapitalization> entry,
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
     * @param holding    holding
     * @param ratingsRaw ratings in string type
     * @param warnings   warnings
     * @return converted ratings map
     */
    Map<EquityMarketCapType, BigDecimal> mapRatingsToRequiredFormat(final Holding holding,
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

    EquityMarketCapType getEquityMarketCapType(final Holding holding,
                                               final String typeStr,
                                               final List<Warning> warnings) {
        final EquityMarketCapType type = EquityMarketCapType.of(typeStr);
        if (isNull(type)) {
            warnings.add(WRN_UNKNOWN_001.warning(holding, typeStr, "FDS Equity Market Capitalization"));
        }
        return type;
    }

}
