package com.fintex.ce.repository.redis;

import com.fintex.ce.model.redis.RHistoricalNavPrices;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.fintex.ce.config.CacheConfig.HISTORICAL_NAV_PRICES;
import static com.fintex.ce.config.RedisConfig.PREFIX_ENV;

@Repository
public interface HistoricalNavPricesRedisRepository extends CoreRedisCacheRepository<RHistoricalNavPrices> {

    @Override
    @Cacheable(value = HISTORICAL_NAV_PRICES, cacheManager = "caffeine1HourCacheManager", unless = "#result == null or #result.size() == 0")
    default List<RHistoricalNavPrices> findAllByHoldingId(final String holdingId) {
        return this.findAllByHoldingIdAndPrefixEnv(holdingId, PREFIX_ENV);
    }

}
