package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.calculation.FixedIncomeStyleboxType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RFixedIncomeStyleboxExposure;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.FixedIncomeStyleboxAllocationSMRepository;
import com.fintex.ce.repository.redis.FixedIncomeStyleboxAllocationRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_FIS_FISE_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.FIXED_INCOME_STYLEBOX_ALLOCATION;
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
public class FixedIncomeStyleboxExposureCacheStorage extends MultipleCacheStorageAbstract<RFixedIncomeStyleboxExposure, RFixedIncomeStyleboxExposure, RFixedIncomeStyleboxExposure, RedisId> {
    static final Map<FixedIncomeStyleboxType, BigDecimal> DEFAULT_MAP;

    static {
        DEFAULT_MAP = Collections.unmodifiableMap(
                Stream.of(FixedIncomeStyleboxType.values()).collect(toMap(type -> type, type -> ZERO))
        );
    }

    public FixedIncomeStyleboxExposureCacheStorage(FixedIncomeStyleboxAllocationSMRepository fdsRepo,
                                                   FixedIncomeStyleboxAllocationRepository fundCanadaCacheRepo,
                                                   FixedIncomeStyleboxAllocationRepository etfCanadaCacheRepo,
                                                   FixedIncomeStyleboxAllocationRepository etfUsCacheRepo,
                                                   CacheStatisticService cacheStatisticService) {
        super(
                fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
                null, cacheStatisticService, FIXED_INCOME_STYLEBOX_ALLOCATION
        );
    }

    @Override
    public Map<Holding, Map<FixedIncomeStyleboxType, BigDecimal>> load(final List<Holding> holdings, final List<DataProvider> providers,
                                                                  final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {

        Map<Holding, Map<FixedIncomeStyleboxType, BigDecimal>> map = new HashMap<>();
        map.putAll(mapForNoneStock(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()), warnings));
        return map;
    }


    <H extends Holding> Map<Holding, Map<FixedIncomeStyleboxType, BigDecimal>> mapForNoneStock(final Map<H, RFixedIncomeStyleboxExposure> holdings,
                                                                                                  final List<Warning> warnings) {
        return holdings.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> fixedIncomeStyleboxExposureMapper(e, warnings)));
    }

    <H extends Holding> Map<FixedIncomeStyleboxType, BigDecimal> fixedIncomeStyleboxExposureMapper(final Map.Entry<H, RFixedIncomeStyleboxExposure> entry,
                                                                                                 final List<Warning> warnings) {
        final Map<FixedIncomeStyleboxType, BigDecimal> map = new EnumMap<>(DEFAULT_MAP);
        if (CollectionUtils.isEmpty(entry.getValue().getBoxValues())) {
            warnings.add(WRN_FIS_FISE_001.warning(entry.getKey()));
            return map;
        }
        entry.getValue().getBoxValues().forEach((typeStr, value) -> {
            final FixedIncomeStyleboxType type = FixedIncomeStyleboxType.of(typeStr);
            if (type == null) {
                warnings.add(WRN_UNKNOWN_001.warning(entry.getKey(), typeStr, "FDS Get Fixed Income Stylebox Exposure"));
            } else {
                map.put(type, value);
            }
        });
        return map;
    }

}
