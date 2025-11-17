package com.fintex.ce.model.redis.equitymarketcapitalization;

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
@RedisHash("REquityMarketCapitalization")
@AllArgsConstructor
public class REquityMarketCapitalization extends RedisId {

    private HoldingType holdingType;
    // Equity Market Capitalization - value
    private Map<String, BigDecimal> ratings;

    public REquityMarketCapitalization() {
        ratings = Map.of();
    }

    public REquityMarketCapitalization(Map<String, BigDecimal> ratings) {
        this.ratings = ratings;
    }
}
