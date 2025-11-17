package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RHistoricalNavPrices;
import com.fintex.ce.repository.graphql.query.HistoricalNavPricesSMRepository;
import com.fintex.ce.repository.redis.HistoricalNavPricesRedisRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.HISTORICAL_NAV_PRICES;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;

@Service
public class HistoryNavPricesCacheStorage extends MultipleCacheStorageAbstract<RHistoricalNavPrices, RHistoricalNavPrices, RHistoricalNavPrices, RHistoricalNavPrices> {

    @Autowired
    public HistoryNavPricesCacheStorage(final HistoricalNavPricesSMRepository queryRepository,
                                        final HistoricalNavPricesRedisRepository monthlyReturnsRepository,
                                        final CacheStatisticService cacheStatisticService) {
        super(
                queryRepository, monthlyReturnsRepository, monthlyReturnsRepository,
                monthlyReturnsRepository, monthlyReturnsRepository, cacheStatisticService, HISTORICAL_NAV_PRICES
        );
    }

    @Override
    public Map<Holding, RHistoricalNavPrices> load(final List<Holding> holdings, final List<DataProvider> providers,
                                                   final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        final Map<Holding, RHistoricalNavPrices> map = new HashMap<>();
        map.putAll(loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of()));
        return map;
    }

}
