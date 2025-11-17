package com.fintex.ce.repository.redis.equitysector;

import com.fintex.ce.model.redis.equitysector.REquitySector;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquitySectorRepository extends CoreRedisCacheRepository<REquitySector> {

}
