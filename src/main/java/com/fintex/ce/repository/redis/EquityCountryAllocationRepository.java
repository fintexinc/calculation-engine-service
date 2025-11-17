package com.fintex.ce.repository.redis;

import com.fintex.ce.model.redis.REquityCountryAllocation;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquityCountryAllocationRepository extends CoreRedisCacheRepository<REquityCountryAllocation> {
}
