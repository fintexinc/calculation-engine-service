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
@RedisHash("CreditQuality")
@AllArgsConstructor
public class RCreditQuality extends RedisId {

    private HoldingType holdingType;
    // credit quality - value
    private Map<String, BigDecimal> ratings;

    public RCreditQuality() {
        ratings = Map.of();
    }
}
