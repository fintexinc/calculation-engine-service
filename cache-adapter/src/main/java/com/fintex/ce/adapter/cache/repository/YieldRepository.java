package com.fintex.ce.adapter.cache.repository;

import com.fintex.ce.adapter.cache.entity.RYield;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YieldRepository extends CoreRedisCacheRepository<RYield> {
}
