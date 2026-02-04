package com.fintex.ce.adapter.cache.repository;

import com.fintex.ce.adapter.cache.entity.REquityStyleboxExposure;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquityStyleboxAllocationRepository extends CoreRedisCacheRepository<REquityStyleboxExposure> {
}
