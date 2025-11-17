package com.fintex.ce.repository.redis.assetallocation;

import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetAllocationRepository extends CoreRedisCacheRepository<RAssetAllocation> {
}
