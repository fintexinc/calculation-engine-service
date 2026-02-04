package com.fintex.ce.adapter.cache.entity.equitymarketcapitalization;

import com.fintex.ce.adapter.cache.entity.core.RedisId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RedisHash("REquityMarketCapitalization")
@NoArgsConstructor
@AllArgsConstructor
public class REquityMarketCapitalizationStock extends RedisId {

  private String styleBox;

}
