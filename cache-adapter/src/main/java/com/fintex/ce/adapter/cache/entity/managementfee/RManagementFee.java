package com.fintex.ce.adapter.cache.entity.managementfee;

import com.fintex.ce.adapter.cache.entity.core.RedisId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("RManagementFee")
@Accessors(chain = true)
public class RManagementFee extends RedisId {

  private BigDecimal managementFee;

}
