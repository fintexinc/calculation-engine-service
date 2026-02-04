package com.fintex.ce.adapter.cache.entity;

import com.fintex.ce.adapter.cache.entity.core.RedisId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@RedisHash("Yield")
public class RYield extends RedisId {
  private BigDecimal dividendYield;
}
