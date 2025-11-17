package com.fintex.ce.model.redis;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.model.redis.core.RedisId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@RedisHash("MonthlyReturns")
@Accessors(chain = true)
public class RMonthlyReturns extends RedisId implements ReturnsI {

    private String currency;
    private HoldingType holdingType;
    private TreeMap<LocalDate, BigDecimal> returns;

}
