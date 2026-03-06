package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.RAssetAllocation;
import com.fintex.ce.domain.model.AssetAllocation;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
public class AssetAllocationMapper implements CacheEntityMapper<AssetAllocation, RAssetAllocation> {

  @Override
  public Optional<AssetAllocation> toDomain(RAssetAllocation entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          AssetAllocation domain = new AssetAllocation();
          domain.setHoldingType(e.getHoldingType());
          if (e.getAssetAllocation() != null) {
            domain.setAssetAllocation(new HashMap<>(e.getAssetAllocation()));
          }
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RAssetAllocation> toEntity(AssetAllocation domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RAssetAllocation entity = new RAssetAllocation();
          entity.setHoldingType(d.getHoldingType());
          if (d.getAssetAllocation() != null) {
            entity.setAssetAllocation(new HashMap<>(d.getAssetAllocation()));
          }
          entity.setHoldingId(d.getHoldingId());
          entity.setProvider(d.getProvider());
          entity.setProviders(d.getProviders());
          return entity;
        });
  }

}
