package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.cache.CacheNameEntity;
import com.fintex.ce.config.enumeration.calculation.FixedIncomeSectorType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.repository.graphql.query.FixedIncomeBondSectorSMRepository;
import com.fintex.ce.repository.redis.fixedincomebondsector.FixedIncomeBondSectorRedisRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_BS_BS_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.config.enumeration.calculation.FixedIncomeSectorType.CORPORATE_BONDS;
import static com.fintex.ce.config.enumeration.calculation.FixedIncomeSectorType.ST_INVESTMENTS;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static com.fintex.ce.util.MapUtils.overrideDefaultValues;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class FixedIncomeBondSectorCacheStorage extends MultipleCacheStorageAbstract<RFixedIncomeBondSecurities, RFixedIncomeBondSecurities, RFixedIncomeBondSecurities, RFixedIncomeBondSecurities> {

    static final Map<FixedIncomeSectorType, BigDecimal> DEFAULT_MAP;

    static {
        DEFAULT_MAP = Collections.unmodifiableMap(
                Stream.of(FixedIncomeSectorType.values()).collect(toMap(type -> type, type -> ZERO))
        );
    }

    @Autowired
    public FixedIncomeBondSectorCacheStorage(final FixedIncomeBondSectorSMRepository queryRepository,
                                             final FixedIncomeBondSectorRedisRepository fixedIncomeBondSectorRedisRepository,
                                             final CacheStatisticService cacheStatisticService) {
        super(queryRepository, fixedIncomeBondSectorRedisRepository, fixedIncomeBondSectorRedisRepository,
                fixedIncomeBondSectorRedisRepository, fixedIncomeBondSectorRedisRepository, cacheStatisticService, CacheNameEntity.ASSET_ALLOCATION);
    }

    @Override
    public Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> load(final List<Holding> holdings, final List<DataProvider> dataProviders,
                                                                     final List<Warning> warnings, final ParamHolderDTO paramHolder) {
        final Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> map = new HashMap<>();
        map.putAll(getCashHoldingValues(holdings));
        map.putAll(mapResponse(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()), warnings));
        map.putAll(mapResponse(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapResponse(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapResponse(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()), warnings));
        map.putAll(addGics(filterHoldings(holdings, GIC_PREDICATE)));
        map.putAll(mapResponse(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapResponse(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapResponse(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapResponse(loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), List.of()), warnings));
        return map;
    }

    private Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> addGics(final List<Holding> holdings) {
        final HashMap<Holding, Map<FixedIncomeSectorType, BigDecimal>> result = new HashMap<>();
        for (final Holding holding : holdings) {
            final GicHolding gic = (GicHolding) holding;
            if (gic.isLessThanOneYearOld()) {
                result.put(holding, Map.of(ST_INVESTMENTS, BigDecimal.ONE));
            } else {
                result.put(holding, Map.of(CORPORATE_BONDS, BigDecimal.ONE));
            }
        }
        return result;
    }

    public Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> getCashHoldingValues(final List<Holding> holdings) {
        return filterHoldings(holdings, CASH_PREDICATE).stream().collect(toMap(h -> h, h -> Map.of(ST_INVESTMENTS, ONE)));
    }

    /**
     * Mapper for non stock holdings
     *
     * @param holdings holdings
     * @param warnings warnings
     * @param <H>      holding type
     * @return mapped response
     */
    <H extends Holding> Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> mapResponse(final Map<H, RFixedIncomeBondSecurities> holdings,
                                                                                         final List<Warning> warnings) {
        return holdings.entrySet().stream().collect(
                toMap(
                        Map.Entry::getKey,
                        e -> convertValues(e, warnings)
                )
        );
    }

    <H extends Holding> Map<FixedIncomeSectorType, BigDecimal> convertValues(final Map.Entry<H, RFixedIncomeBondSecurities> entry,
                                                                             final List<Warning> warnings) {
        final Map<String, BigDecimal> ratingsRaw = entry.getValue().getFixedIncomeBondSectors();
        if (CollectionUtils.isEmpty(ratingsRaw)) {
            warnings.add(WRN_BS_BS_001.warning(entry.getKey()));
            return DEFAULT_MAP;
        }
        return mapToRequiredFormat(entry.getKey(), ratingsRaw, warnings);
    }

    /**
     * Converts rating from string to required Enum type for non Stocks holdings
     *
     * @param holding    holding
     * @param ratingsRaw ratings in string type
     * @param warnings   warnings
     * @return converted ratings map
     */
    Map<FixedIncomeSectorType, BigDecimal> mapToRequiredFormat(final Holding holding,
                                                               final Map<String, BigDecimal> ratingsRaw,
                                                               final List<Warning> warnings) {
        final Map<FixedIncomeSectorType, BigDecimal> returns = new EnumMap<>(FixedIncomeSectorType.class);
        ratingsRaw.forEach((typeStr, value) -> {
            final FixedIncomeSectorType type = getFixedIncomeSectorType(holding, typeStr, warnings);
            if (!Objects.isNull(type)) {
                returns.put(type, value);
            }
        });
        return overrideDefaultValues(DEFAULT_MAP, returns);
    }

    FixedIncomeSectorType getFixedIncomeSectorType(final Holding holding,
                                                   final String typeStr,
                                                   final List<Warning> warnings) {
        final FixedIncomeSectorType type = FixedIncomeSectorType.of(typeStr);
        if (Objects.isNull(type)) {
            warnings.add(WRN_UNKNOWN_001.warning(holding, typeStr, "FDS Fixed Income Sector Allocation"));
        }
        return type;
    }

}
