package com.fintex.ce.repository.redis.commonholdings;

import com.fintex.ce.model.redis.topcommonholdings.RCommonHoldingsStock;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

import static com.fintex.ce.config.CacheConfig.TOP_COMMON_HOLDINGS_STOCK;
import static com.fintex.ce.config.RedisConfig.PREFIX_ENV;

public interface CommonHoldingsStockRepository extends CoreRedisCacheRepository<RCommonHoldingsStock> {

    @Override
    @Cacheable(value = TOP_COMMON_HOLDINGS_STOCK, cacheManager = "caffeine1HourCacheManager", unless = "#result == null or #result.size() == 0")
    default List<RCommonHoldingsStock> findAllByHoldingId(final String holdingId) {
        return this.findAllByHoldingIdAndPrefixEnv(holdingId, PREFIX_ENV);
    }

}
