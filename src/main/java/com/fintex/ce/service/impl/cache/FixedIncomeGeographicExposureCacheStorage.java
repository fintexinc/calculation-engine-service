package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RCountryExposure;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.CountryExposureSMRepository;
import com.fintex.ce.repository.redis.CountryExposureRepository;
import com.fintex.ce.service.impl.GeographicAllocationMappingServiceImpl;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_FICQ_BCE_001;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.COUNTRY_EXPOSURE;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static java.util.stream.Collectors.toMap;

@Service
public class FixedIncomeGeographicExposureCacheStorage
        extends MultipleCacheStorageAbstract<RCountryExposure, RCountryExposure, RCountryExposure, RedisId> {

    private final GeographicAllocationMappingServiceImpl geographicAllocationMappingService;

    public FixedIncomeGeographicExposureCacheStorage(CountryExposureSMRepository fdsRepo,
                                                     CountryExposureRepository fundCanadaCacheRepo,
                                                     CountryExposureRepository etfCanadaCacheRepo,
                                                     CountryExposureRepository etfUsCacheRepo,
                                                     CacheStatisticService cacheStatisticService,
                                                     GeographicAllocationMappingServiceImpl geographicAllocationMappingService) {
        super(
                fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo,
                null, cacheStatisticService, COUNTRY_EXPOSURE
        );
        this.geographicAllocationMappingService = geographicAllocationMappingService;
    }

    @Override
    public Map<Holding, Map<GeographicRegionType, BigDecimal>> load(final List<Holding> holdings, final List<DataProvider> providers,
                                                                    final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        Map<Holding, Map<GeographicRegionType, BigDecimal>> map = new HashMap<>();
        map.putAll(mapper(loadForBenchOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadForBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadForBenchOfBenchmarks(filterHoldings(holdings, BENCHMARKS_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), List.of()), warnings));
        map.putAll(mapper(loadUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), List.of()), warnings));
        map.putAll(addGics(filterHoldings(holdings, GIC_PREDICATE)));
        return map;
    }

    private Map<Holding, Map<GeographicRegionType, BigDecimal>> addGics(final List<Holding> holdings) {
        final HashMap<Holding, Map<GeographicRegionType, BigDecimal>> result = new HashMap<>();
        for (final Holding holding : holdings) {
            final GicHolding gic = (GicHolding) holding;
            if (!gic.isLessThanOneYearOld()) {
                result.put(holding, Map.of(GeographicRegionType.CANADA, BigDecimal.ONE));
            }
        }
        return result;
    }

    private <H extends Holding> Map<Holding, Map<GeographicRegionType, BigDecimal>> mapper(final Map<H, RCountryExposure> holdings,
                                                                                           final List<Warning> warnings) {
        final Map<Holding, Map<String, BigDecimal>> mappedHoldings = holdings.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
        return geographicAllocationMappingService.mapToGeographicRegions(mappedHoldings, warnings, WRN_FICQ_BCE_001);
    }

}
