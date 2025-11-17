package com.fintex.ce.repository.redis.fixedincomebondsector;

import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import jdk.jfr.Registered;

@Registered
public interface FixedIncomeBondSectorRedisRepository extends CoreRedisCacheRepository<RFixedIncomeBondSecurities> {
}
