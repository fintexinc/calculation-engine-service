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
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@RedisHash("ClassificationAllocation")
public class RClassificationAllocation extends RedisId {

    private HoldingType holdingType;

    private Map<String, BigDecimal> securityClassificationValues;

    public RClassificationAllocation() {
        securityClassificationValues = Map.of();
    }

}
