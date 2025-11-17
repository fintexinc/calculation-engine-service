package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.calculation.MaturityAllocationType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RMaturityAllocation;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.MaturityAllocationSMRepository;
import com.fintex.ce.repository.redis.MaturityAllocationRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_MA_MA_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MATURITY_ALLOCATION;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class MaturityAllocationCacheStorage extends MultipleCacheStorageAbstract<RMaturityAllocation, RMaturityAllocation, RMaturityAllocation, RedisId> {


    static final Map<MaturityAllocationType, BigDecimal> DEFAULT_MAP;

    static {
        DEFAULT_MAP = Collections.unmodifiableMap(
                Stream.of(MaturityAllocationType.values()).collect(toMap(type -> type, type -> ZERO))
        );
    }

    public MaturityAllocationCacheStorage(MaturityAllocationSMRepository fdsRepo,
                                          MaturityAllocationRepository fundCanadaCacheRepo,
                                          MaturityAllocationRepository etfCanadaCacheRepo,
                                          MaturityAllocationRepository etfUsCacheRepo,
                                          CacheStatisticService cacheStatisticService) {
        super(
                fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
                null, cacheStatisticService, MATURITY_ALLOCATION
        );
    }

    @Override
    public Map<Holding, Map<MaturityAllocationType, BigDecimal>> load(final List<Holding> holdings, final List<DataProvider> providers,
                                                                  final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {

        Map<Holding, Map<MaturityAllocationType, BigDecimal>> map = new HashMap<>();
        map.putAll(mapForNoneStock(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapForNoneStock(loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), List.of()), warnings));
        return map;
    }

    <H extends Holding> Map<Holding, Map<MaturityAllocationType, BigDecimal>> mapForNoneStock(final Map<H, RMaturityAllocation> holdings,
                                                                                                  final List<Warning> warnings) {
        return holdings.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> maturityAllocationMapper(e, warnings)));
    }

    <H extends Holding> Map<MaturityAllocationType, BigDecimal> maturityAllocationMapper(final Map.Entry<H, RMaturityAllocation> entry,
                                                                                                 final List<Warning> warnings) {
        final Map<MaturityAllocationType, BigDecimal> map = new EnumMap<>(DEFAULT_MAP);
        if (CollectionUtils.isEmpty(entry.getValue().getMaturityDurationValues())) {
            warnings.add(WRN_MA_MA_001.warning(entry.getKey()));
            return map;
        }
        entry.getValue().getMaturityDurationValues().forEach((typeStr, value) -> {
            final MaturityAllocationType type = MaturityAllocationType.of(typeStr);
            if (type == null) {
                warnings.add(WRN_UNKNOWN_001.warning(entry.getKey(), typeStr, "FDS Get Maturity Allocation"));
            } else {
                map.merge(type.getDisplayType(), value, BigDecimal::add);
            }
        });
        return map;
    }

}
