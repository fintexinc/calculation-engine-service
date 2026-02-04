package com.fintex.ce.adapter.cache.statistic;

import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.cache.entity.core.RedisId;

import java.util.Map;

public interface CacheStatisticService {

  <H extends Holding, R extends RedisId> void analyse(
      final Map<H, R> responses, final CacheNameEntity cacheNameEntity, final CacheCategory cacheCategory);

}
