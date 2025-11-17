package com.fintex.ce.model.redis;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.model.redis.core.RedisId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RedisHash("EquityCountryAllocation")
@AllArgsConstructor
public class REquityCountryAllocation extends RedisId {

    private HoldingType holdingType;
    // country id - value
    private Map<String, BigDecimal> allocations;

    public REquityCountryAllocation() {
        allocations = Map.of();
    }
}
