package com.fintex.ce.model.redis;

import com.fintex.ce.model.redis.core.RedisId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@RedisHash("Yield")
public class RYield extends RedisId {
    private BigDecimal dividendYield;
}
