package com.fintex.ce.repository.redis;

import com.fintex.ce.model.redis.RYield;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YieldRepository extends CoreRedisCacheRepository<RYield> {
}
