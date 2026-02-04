package com.fintex.ce.adapter.cache.entity;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@RedisHash("FixedIncomeBondSecurities")
@Accessors(chain = true)
public class RFixedIncomeBondSecurities extends RedisId {

  private HoldingType holdingType;
  private Map<String, BigDecimal> fixedIncomeBondSectors;

  public RFixedIncomeBondSecurities() {
  }

  public RFixedIncomeBondSecurities(final HoldingType holdingType,
      final Map<String, BigDecimal> fixedIncomeBondSectors) {
    this.holdingType = holdingType;
    this.fixedIncomeBondSectors = fixedIncomeBondSectors;
  }

}
