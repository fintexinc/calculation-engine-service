package com.fintex.ce.repository.redis;

import com.fintex.ce.model.redis.RHistoricalDistributions;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.fintex.ce.config.CacheConfig.HISTORICAL_DISTRIBUTIONS;
import static com.fintex.ce.config.RedisConfig.PREFIX_ENV;

@Repository
public interface HistoricalDistributionsRedisRepository extends CoreRedisCacheRepository<RHistoricalDistributions> {

    @Override
    @Cacheable(value = HISTORICAL_DISTRIBUTIONS, cacheManager = "caffeine1HourCacheManager", unless = "#result == null or #result.size() == 0")
    default List<RHistoricalDistributions> findAllByHoldingId(final String holdingId) {
        return this.findAllByHoldingIdAndPrefixEnv(holdingId, PREFIX_ENV);
    }

}
