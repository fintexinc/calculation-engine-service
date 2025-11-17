package com.fintex.ce.repository.redis;

import com.fintex.ce.model.redis.RSalesCharge;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesChargeRepository extends CoreRedisCacheRepository<RSalesCharge> {
}
