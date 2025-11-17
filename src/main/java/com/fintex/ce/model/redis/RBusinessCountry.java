package com.fintex.ce.model.redis;

import com.fintex.ce.model.redis.core.RedisId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@RedisHash("BusinessCountry")
@Accessors(chain = true)
public class RBusinessCountry extends RedisId {

    private String value;
}
