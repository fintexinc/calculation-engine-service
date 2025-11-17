package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.calculation.CreditQualityRating;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RCreditQuality;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.CreditQualitySMRepository;
import com.fintex.ce.repository.redis.CreditQualityRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_CQ_CQ_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.CREDIT_QUALITY;
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
public class CreditQualityCacheStorage
        extends MultipleCacheStorageAbstract<RCreditQuality, RCreditQuality, RCreditQuality, RedisId> {

    public CreditQualityCacheStorage(CreditQualitySMRepository fdsRepo,
                                     CreditQualityRepository fundCanadaCacheRepo,
                                     CreditQualityRepository etfCanadaCacheRepo,
                                     CreditQualityRepository etfUsCacheRepo,
                                     CacheStatisticService cacheStatisticService) {
        super(
                fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
                null, cacheStatisticService, CREDIT_QUALITY
        );
    }

    @Override
    public Map<Holding, Map<CreditQualityRating, BigDecimal>> load(final List<Holding> holdings, final List<DataProvider> providers,
                                                                   final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        Map<Holding, Map<CreditQualityRating, BigDecimal>> map = new HashMap<>();
        map.putAll(mapper(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadBenchOfFixedIncomes(filterHoldings(holdings, FIXED_INCOME_PREDICATE), List.of()), warnings));
        map.putAll(addGics(filterHoldings(holdings, GIC_PREDICATE)));
        return map;
    }

    Map<Holding, Map<CreditQualityRating, BigDecimal>> addGics(final List<Holding> holdings) {
        final HashMap<Holding, Map<CreditQualityRating, BigDecimal>> result = new HashMap<>();
        for (final Holding holding : holdings) {
            final GicHolding gic = (GicHolding) holding;
            if(!gic.isLessThanOneYearOld()) {
                result.put(holding, Map.of(CreditQualityRating.AAA, BigDecimal.ONE));
            }
        }
        return result;
    }

    <H extends Holding> Map<Holding, Map<CreditQualityRating, BigDecimal>> mapper(final Map<H, RCreditQuality> holdings,
                                                                                  final List<Warning> warnings) {
        return holdings.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> mapRatings(e.getKey(), e.getValue(), warnings)));
    }

    Map<CreditQualityRating, BigDecimal> mapRatings(final Holding holding, final RCreditQuality creditQuality, final List<Warning> warnings) {
        if (CollectionUtils.isEmpty(creditQuality.getRatings())) {
            warnings.add(WRN_CQ_CQ_001.warning(holding));
            return Map.of();
        }
        final Map<CreditQualityRating, BigDecimal> map = new EnumMap<>(CreditQualityRating.class);
        creditQuality.getRatings().forEach((ratingStr, value) -> {
            final CreditQualityRating rating = CreditQualityRating.of(ratingStr);
            if (rating == null) {
                warnings.add(WRN_UNKNOWN_001.warning(holding, ratingStr, "Credit Quality"));
            } else {
                map.put(rating, value);
            }
        });
        return map;
    }

}
