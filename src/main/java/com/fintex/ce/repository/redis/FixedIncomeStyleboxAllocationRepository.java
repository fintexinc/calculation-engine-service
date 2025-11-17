package com.fintex.ce.repository.redis;

import com.fintex.ce.model.redis.RFixedIncomeStyleboxExposure;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FixedIncomeStyleboxAllocationRepository extends CoreRedisCacheRepository<RFixedIncomeStyleboxExposure> {
}
