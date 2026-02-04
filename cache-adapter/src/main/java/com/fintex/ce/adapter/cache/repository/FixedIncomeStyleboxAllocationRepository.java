package com.fintex.ce.adapter.cache.repository;

import com.fintex.ce.adapter.cache.entity.RFixedIncomeStyleboxExposure;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FixedIncomeStyleboxAllocationRepository
    extends
      CoreRedisCacheRepository<RFixedIncomeStyleboxExposure> {
}
