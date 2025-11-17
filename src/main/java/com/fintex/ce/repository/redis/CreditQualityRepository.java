package com.fintex.ce.repository.redis;

import com.fintex.ce.model.redis.RCreditQuality;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditQualityRepository extends CoreRedisCacheRepository<RCreditQuality> {
}
