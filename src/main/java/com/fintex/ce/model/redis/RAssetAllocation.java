package com.fintex.ce.model.redis;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.model.redis.core.RedisId;
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
@RedisHash("AssetAllocation")
@Accessors(chain = true)
public class RAssetAllocation extends RedisId {

    private HoldingType holdingType;
    private Map<String, BigDecimal> assetAllocation;

    public RAssetAllocation() {
    }

    public RAssetAllocation(HoldingType holdingType, Map<String, BigDecimal> assetAllocation) {
        this.holdingType = holdingType;
        this.assetAllocation = assetAllocation;
    }
}
