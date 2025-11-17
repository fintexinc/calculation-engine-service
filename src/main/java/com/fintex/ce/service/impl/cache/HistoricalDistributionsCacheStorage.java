package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RHistoricalDistributions;
import com.fintex.ce.repository.graphql.query.HistoricalDistributionsSMRepository;
import com.fintex.ce.repository.redis.HistoricalDistributionsRedisRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.HISTORICAL_DISTRIBUTIONS;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;

@Service
public class HistoricalDistributionsCacheStorage extends MultipleCacheStorageAbstract<RHistoricalDistributions, RHistoricalDistributions, RHistoricalDistributions, RHistoricalDistributions> {

    @Autowired
    public HistoricalDistributionsCacheStorage(final HistoricalDistributionsSMRepository fdsRepository,
                                               final HistoricalDistributionsRedisRepository redisRepository,
                                               final CacheStatisticService cacheStatisticService) {
        super(
                fdsRepository, redisRepository, redisRepository,
                redisRepository, redisRepository, cacheStatisticService, HISTORICAL_DISTRIBUTIONS
        );
    }

    @Override
    public Map<Holding, RHistoricalDistributions> load(final List<Holding> holdings, final List<DataProvider> providers,
                                                       final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        final Map<Holding, RHistoricalDistributions> map = new HashMap<>();
        map.putAll(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()));
        return map;
    }

}
