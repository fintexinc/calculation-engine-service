package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.topcommonholdings.RCommonHoldings;
import com.fintex.ce.domain.model.CommonHoldingsDTO;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.util.JacksonUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CommonHoldingsMapper implements CacheEntityMapper<CommonHoldings, RCommonHoldings> {

  @Override
  public Optional<CommonHoldings> toDomain(RCommonHoldings entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          List<CommonHoldingsDTO> dtoList = e.getHoldings();
          CommonHoldings domain = new CommonHoldings();
          domain.setHoldings(mapHoldingsToDomain(dtoList));
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RCommonHoldings> toEntity(CommonHoldings domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          List<CommonHoldingsDTO> dtoList = mapHoldingsToDto(d.getHoldings());
          String serialized = JacksonUtil.serialize(dtoList);
          return new RCommonHoldings(serialized);
        });
  }

  private List<CommonHoldings.CommonHolding> mapHoldingsToDomain(List<CommonHoldingsDTO> dtoList) {
    if (dtoList == null) {
      return null;
    }
    return dtoList.stream()
        .map(this::mapHoldingToDomain)
        .collect(Collectors.toList());
  }

  private CommonHoldings.CommonHolding mapHoldingToDomain(CommonHoldingsDTO dto) {
    if (dto == null) {
      return null;
    }
    return new CommonHoldings.CommonHolding(
        dto.getName(),
        dto.getCompanyName(),
        dto.getType(),
        dto.getValue(),
        dto.getUnderlyingHoldings() != null ? mapHoldingsToDomain(dto.getUnderlyingHoldings()) : null,
        dto.getTicker(),
        dto.getExchangeCode(),
        dto.getWeight(),
        dto.getUuid());
  }

  private List<CommonHoldingsDTO> mapHoldingsToDto(List<CommonHoldings.CommonHolding> domainList) {
    if (domainList == null) {
      return null;
    }
    return domainList.stream()
        .map(this::mapHoldingToDto)
        .collect(Collectors.toList());
  }

  private CommonHoldingsDTO mapHoldingToDto(CommonHoldings.CommonHolding domain) {
    if (domain == null) {
      return null;
    }
    CommonHoldingsDTO dto = new CommonHoldingsDTO();
    dto.setName(domain.getName());
    dto.setCompanyName(domain.getCompanyName());
    dto.setType(domain.getType());
    dto.setValue(domain.getValue());
    dto.setUnderlyingHoldings(domain.getUnderlyingHoldings() != null
        ? mapHoldingsToDto(domain.getUnderlyingHoldings())
        : null);
    dto.setTicker(domain.getTicker());
    dto.setExchangeCode(domain.getExchangeCode());
    dto.setWeight(domain.getWeight());
    dto.setUuid(domain.getUuid());
    return dto;
  }

}
