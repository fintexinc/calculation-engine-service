package com.fintex.ce.adapter.cache.repository.assetallocation;

import com.fintex.ce.adapter.cache.entity.RAssetAllocation;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetAllocationRepository extends CoreRedisCacheRepository<RAssetAllocation> {
}
