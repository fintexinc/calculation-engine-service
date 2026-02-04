package com.fintex.ce.adapter.cache.repository;

import com.fintex.ce.adapter.cache.entity.RMaturityAllocation;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaturityAllocationRepository extends CoreRedisCacheRepository<RMaturityAllocation> {
}
