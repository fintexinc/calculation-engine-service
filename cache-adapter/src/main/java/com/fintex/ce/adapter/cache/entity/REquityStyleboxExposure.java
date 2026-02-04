package com.fintex.ce.adapter.cache.entity;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@RedisHash("EquityStyleboxExposure")
public class REquityStyleboxExposure extends RedisId {

  private HoldingType holdingType;

  private Map<String, BigDecimal> boxValues;

  public REquityStyleboxExposure() {
    boxValues = Map.of();
  }

}
