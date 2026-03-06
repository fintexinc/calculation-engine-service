package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.managementfee.RManagementFee;
import com.fintex.ce.domain.model.ManagementFee;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ManagementFeeMapper implements CacheEntityMapper<ManagementFee, RManagementFee> {

  @Override
  public Optional<ManagementFee> toDomain(RManagementFee entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          ManagementFee domain = new ManagementFee();
          domain.setManagementFee(e.getManagementFee());
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RManagementFee> toEntity(ManagementFee domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RManagementFee entity = new RManagementFee();
          entity.setManagementFee(d.getManagementFee());
          return entity;
        });
  }

}
