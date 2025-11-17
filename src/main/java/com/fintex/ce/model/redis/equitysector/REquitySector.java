package com.fintex.ce.model.redis.equitysector;

import com.fintex.ce.model.redis.core.RedisId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RedisHash("EquitySector")
@AllArgsConstructor
public class REquitySector extends RedisId {

    private Map<String, BigDecimal> allocations;

    public REquitySector() {
        allocations = new HashMap<>();
    }
}
