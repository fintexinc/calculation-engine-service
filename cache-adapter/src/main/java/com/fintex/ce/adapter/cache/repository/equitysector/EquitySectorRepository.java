package com.fintex.ce.adapter.cache.repository.equitysector;

import com.fintex.ce.adapter.cache.entity.equitysector.REquitySector;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquitySectorRepository extends CoreRedisCacheRepository<REquitySector> {

}
