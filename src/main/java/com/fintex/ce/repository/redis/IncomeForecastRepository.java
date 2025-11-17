package com.fintex.ce.repository.redis;

import com.fintex.ce.model.redis.RIncomeForecast;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeForecastRepository extends CoreRedisCacheRepository<RIncomeForecast> {
}
