package com.fintex.ce.model.redis.topcommonholdings;

import com.fintex.ce.model.redis.core.RedisId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RedisHash("TopCommonHoldingsStock")
@AllArgsConstructor
@NoArgsConstructor
public class RCommonHoldingsStock extends RedisId {

    private String companyName;
    private String ticker;
    private String exchangeCode;
}
