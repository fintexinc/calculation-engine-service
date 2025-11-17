package com.fintex.ce.service.interfaces.cache.statistic;

import com.fintex.ce.config.enumeration.cache.CacheCategory;
import com.fintex.ce.config.enumeration.cache.CacheNameEntity;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.model.redis.core.RedisId;

import java.util.Map;

public interface CacheStatisticService {

    <H extends Holding, R extends RedisId> void analyse(
            final Map<H, R> responses, final CacheNameEntity cacheNameEntity, final CacheCategory cacheCategory);

}
