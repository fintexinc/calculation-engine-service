package com.fintex.ce.adapter.cache.repository.fixedincomebondsector;

import com.fintex.ce.adapter.cache.entity.RFixedIncomeBondSecurities;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import jdk.jfr.Registered;

@Registered
public interface FixedIncomeBondSectorRedisRepository extends CoreRedisCacheRepository<RFixedIncomeBondSecurities> {
}
